package io.onedev.server.entitymanager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.Permission;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.CloneCommand;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.Usage;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.avatar.AvatarManager;

@Singleton
public class DefaultProjectManager extends AbstractEntityManager<Project> implements ProjectManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);
	
    private final CommitInfoManager commitInfoManager;
    
    private final BuildManager buildManager;
    
    private final GroupManager groupManager;
    
    private final AvatarManager avatarManager;
    
    private final SettingManager settingManager;
    
    private final SessionManager sessionManager;
    
    private final TransactionManager transactionManager;
    
    private final ListenerRegistry listenerRegistry;
    
    private final String gitReceiveHook;
    
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultProjectManager(Dao dao, CommitInfoManager commitInfoManager,  
    		BuildManager buildManager, AvatarManager avatarManager, GroupManager groupManager,
    		SettingManager settingManager, TransactionManager transactionManager, 
    		SessionManager sessionManager, ListenerRegistry listenerRegistry) {
    	super(dao);
    	
        this.commitInfoManager = commitInfoManager;
        this.buildManager = buildManager;
        this.groupManager = groupManager;
        this.avatarManager = avatarManager;
        this.settingManager = settingManager;
        this.transactionManager = transactionManager;
        this.sessionManager = sessionManager;
        this.listenerRegistry = listenerRegistry;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is, Charset.defaultCharset()), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    
    @Override
    public Repository getRepository(Project project) {
    	Repository repository = repositoryCache.get(project.getId());
    	if (repository == null) {
    		synchronized (repositoryCache) {
    			repository = repositoryCache.get(project.getId());
    			if (repository == null) {
    				try {
						repository = new FileRepository(project.getGitDir());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
    				repositoryCache.put(project.getId(), repository);
    			}
    		}
    	}
    	return repository;
    }
    
    @Override
    public void save(Project project) {
    	save(project, null);
    }
    
    @Transactional
    @Override
    public void save(Project project, String oldName) {
    	boolean isNew = project.isNew();
    	
    	dao.persist(project);
    	
       	if (isNew) {
       		project.setOwner(SecurityUtils.getUser());
           	checkSanity(project);
    	} 
       	
    	if (oldName != null && !oldName.equals(project.getName())) {
        	for (JobExecutor jobExecutor: settingManager.getJobExecutors())
        		jobExecutor.onRenameProject(oldName, project.getName());
        	for (GroovyScript groovyScript: settingManager.getGroovyScripts())
        		groovyScript.onRenameProject(oldName, project.getName());
    	}
    	
    }
    
    @Transactional
    @Override
    public void delete(Project project) {
    	Usage usage = new Usage();
    	int index = 0;
    	for (JobExecutor jobExecutor: settingManager.getJobExecutors()) {
    		usage.add(jobExecutor.onDeleteProject(project.getName(), index).prefix("administration"));
    		index++;
    	}
    	for (GroovyScript groovyScript: settingManager.getGroovyScripts()) {
    		usage.add(groovyScript.onDeleteProject(project.getName(), index).prefix("administration"));
    		index++;
    	}
    	
    	usage.checkInUse("Project '" + project.getName() + "'");

    	for (Build build: project.getBuilds()) 
    		buildManager.delete(build);
    	
    	Query<?> query = getSession().createQuery("update Project set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", project);
    	query.executeUpdate();

    	dao.remove(project);
    	
    	synchronized (repositoryCache) {
			Repository repository = repositoryCache.remove(project.getId());
			if (repository != null) 
				repository.close();
		}
    }
    
    @Override
    public Project find(String projectName) {
		EntityCriteria<Project> criteria = newCriteria();
		criteria.add(Restrictions.eq("name", projectName));
		criteria.setCacheable(true);
		return find(criteria);
    }

    @Transactional
	@Override
	public void fork(Project from, Project to) {
    	dao.persist(to);
    	to.setOwner(SecurityUtils.getUser());
        FileUtils.cleanDir(to.getGitDir());
        new CloneCommand(to.getGitDir()).mirror(true).from(from.getGitDir().getAbsolutePath()).call();
        checkSanity(to);
        commitInfoManager.cloneInfo(from, to);
        avatarManager.copyAvatar(from, to);
	}

	private boolean isGitHookValid(File gitDir, String hookName) {
        File hookFile = new File(gitDir, "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile, Charset.defaultCharset());
			if (!content.contains("ENV_GIT_ALTERNATE_OBJECT_DIRECTORIES"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	private void checkSanity(Project project) {
		File gitDir = project.getGitDir();
		if (gitDir.listFiles().length == 0) {
        	logger.info("Initializing git repository in '" + gitDir + "'...");
            try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		} else if (!GitUtils.isValid(gitDir)) {
        	logger.warn("Directory '" + gitDir + "' is not a valid git repository, reinitializing...");
        	FileUtils.cleanDir(gitDir);
            try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
        } 

		if (!isGitHookValid(gitDir, "pre-receive") || !isGitHookValid(gitDir, "post-receive")) {
            File hooksDir = new File(gitDir, "hooks");

            File gitPreReceiveHookFile = new File(hooksDir, "pre-receive");
            FileUtils.writeFile(gitPreReceiveHookFile, String.format(gitReceiveHook, "git-prereceive-callback"));
            gitPreReceiveHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, String.format(gitReceiveHook, "git-postreceive-callback"));
            gitPostReceiveHookFile.setExecutable(true);
        }

		try {
			StoredConfig config = project.getRepository().getConfig();
			boolean changed = false;
			if (config.getEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, ConfigConstants.CONFIG_KEY_ALGORITHM, 
					SupportedAlgorithm.MYERS) != SupportedAlgorithm.HISTOGRAM) {
				config.setEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, ConfigConstants.CONFIG_KEY_ALGORITHM, 
						SupportedAlgorithm.HISTOGRAM);
				changed = true;
			}
			if (!config.getBoolean("uploadpack", "allowAnySHA1InWant", false)) {
				config.setBoolean("uploadpack", null, "allowAnySHA1InWant", true);
				changed = true;
			}
			if (changed)
				config.save();				
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Listen
	public void on(SystemStopping event) {
		synchronized(repositoryCache) {
			for (Repository repository: repositoryCache.values()) {
				repository.close();
			}
		}
	}

	@Listen
	public void on(SystemStarted event) {
		logger.info("Checking projects...");
		for (Project project: query())
			checkSanity(project);
	}

	@Transactional
	@Override
	public void onDeleteBranch(Project project, String branchName) {
		Usage usage = new Usage();

		for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
			BranchProtection protection = it.next();
			PatternSet patternSet = PatternSet.fromString(protection.getBranches());
			patternSet.getIncludes().remove(branchName);
			patternSet.getExcludes().remove(branchName);
			protection.setBranches(patternSet.toString());
			if (protection.getBranches().length() == 0)
				it.remove();
		}
		
		usage.add(project.getIssueSetting().onDeleteBranch(branchName));
		
		usage.prefix("project setting").checkInUse("Branch '" + branchName + "'");
	}
	
	@Transactional
	@Override
	public void deleteBranch(Project project, String branchName) {
		onDeleteBranch(project, branchName);

		String refName = GitUtils.branch2ref(branchName);
    	ObjectId commitId = project.getObjectId(refName, true);
    	try {
			project.git().branchDelete().setForce(true).setBranchNames(branchName).call();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
    	
    	Long projectId = project.getId();
    	transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
		    	sessionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						Project project = load(projectId);
						listenerRegistry.post(new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
					}
		    		
		    	}, SecurityUtils.getSubject());
			}
    		
    	});
		
	}

	@Transactional
	@Override
	public void onDeleteTag(Project project, String tagName) {
		Usage usage = new Usage();
		for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) { 
			TagProtection protection = it.next();
			PatternSet patternSet = PatternSet.fromString(protection.getTags());
			patternSet.getIncludes().remove(tagName);
			patternSet.getExcludes().remove(tagName);
			protection.setTags(patternSet.toString());
			if (protection.getTags().length() == 0)
				it.remove();
		}
		
		usage.prefix("project setting").checkInUse("Tag '" + tagName + "'");
	}
	
	@Transactional
	@Override
	public void deleteTag(Project project, String tagName) {
    	onDeleteTag(project, tagName);
    	
    	String refName = GitUtils.tag2ref(tagName);
    	ObjectId commitId = project.getRevCommit(refName, true).getId();
    	try {
			project.git().tagDelete().setTags(tagName).call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}

    	Long projectId = project.getId();
    	transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
		    	sessionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						Project project = load(projectId);
						listenerRegistry.post(new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
					}
		    		
		    	}, SecurityUtils.getSubject());
			}
    		
    	});
	}
	
	@Override
	public List<Project> query() {
		return query(true);
	}

	@Override
	public Collection<Project> getPermittedProjects(User user, Permission permission) {
		Collection<Project> projects = new HashSet<>();
		
		if (SecurityUtils.isAdministrator()) {
			projects.addAll(query());
		} else {
			if (user != null) {
				for (Membership membership: user.getMemberships()) {
					for (GroupAuthorization authorization: membership.getGroup().getProjectAuthorizations()) {
						if (authorization.getRole().implies(permission))
							projects.add(authorization.getProject());
					}
				}
				for (UserAuthorization authorization: user.getProjectAuthorizations()) { 
					if (authorization.getRole().implies(permission))
						projects.add(authorization.getProject());
				}
			}
			Group group = groupManager.findAnonymous();
			if (group != null) {
				for (GroupAuthorization authorization: group.getProjectAuthorizations()) { 
					if (authorization.getRole().implies(permission))
						projects.add(authorization.getProject());
				}
			}
		}
		
		return projects;
	}

}
