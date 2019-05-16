package io.onedev.server.entitymanager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.cache.CacheManager;
import io.onedev.server.cache.CommitInfoManager;
import io.onedev.server.ci.JobDependency;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.CloneCommand;
import io.onedev.server.git.command.ListChangedFilesCommand;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.FileProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserAuthorizationFacade;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;
import io.onedev.server.web.avatar.AvatarManager;

@Singleton
public class DefaultProjectManager extends AbstractEntityManager<Project> implements ProjectManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);
	
    private final CommitInfoManager commitInfoManager;
    
    private final UserAuthorizationManager userAuthorizationManager;
    
    private final BuildManager buildManager;
    
    private final CacheManager cacheManager;
    
    private final AvatarManager avatarManager;
    
    private final String gitReceiveHook;
    
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultProjectManager(Dao dao, CommitInfoManager commitInfoManager,  
    		UserAuthorizationManager userAuthorizationManager, BuildManager buildManager, 
    		CacheManager cacheManager, AvatarManager avatarManager) {
    	super(dao);
    	
        this.commitInfoManager = commitInfoManager;
        this.userAuthorizationManager = userAuthorizationManager;
        this.buildManager = buildManager;
        this.cacheManager = cacheManager;
        this.avatarManager = avatarManager;
        
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
    		UserAuthorization authorization = new UserAuthorization();
    		authorization.setPrivilege(ProjectPrivilege.ADMINISTRATION);
    		authorization.setProject(project);
    		authorization.setUser(SecurityUtils.getUser());
    		userAuthorizationManager.save(authorization);
           	checkSanity(project);
    	}
    }
    
    @Transactional
    @Override
    public void delete(Project project) {
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
    	Long id = cacheManager.getProjectIdByName(projectName);
    	if (id != null)
    		return load(id);
    	else
    		return null;
    }

    @Transactional
	@Override
	public void fork(Project from, Project to) {
    	save(to);
        FileUtils.cleanDir(to.getGitDir());
        new CloneCommand(to.getGitDir()).mirror(true).from(from.getGitDir().getAbsolutePath()).call();
        commitInfoManager.cloneInfo(from, to);
        avatarManager.copyAvatar(from.getFacade(), to.getFacade());
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
	@Listen
	public void on(RefUpdated event) {
		if (event.getNewCommitId().equals(ObjectId.zeroId())) {
			Project project = event.getProject();
			String branch = GitUtils.ref2branch(event.getRefName());
			if (branch != null) {
				for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) {
					if (it.next().onBranchDeleted(branch))	
						it.remove();
				}
			}
			String tag = GitUtils.ref2tag(event.getRefName());
			if (tag != null) {
				for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) {
					if (it.next().onTagDeleted(tag))	
						it.remove();
				}
			}
		}
	}

	@Override
	public Collection<ProjectFacade> getAccessibleProjects(User user) {
		Collection<ProjectFacade> projects = new HashSet<>();
		
		if (SecurityUtils.isAdministrator()) {
			projects.addAll(cacheManager.getProjects().values());
		} else {
			if (user != null) {
				Collection<Long> groupIds = new HashSet<>();
				for (MembershipFacade membership: cacheManager.getMemberships().values()) {
					if (membership.getUserId().equals(user.getId())) 
						groupIds.add(membership.getGroupId());
				}
				for (GroupAuthorizationFacade authorization: cacheManager.getGroupAuthorizations().values()) {
					if (groupIds.contains(authorization.getGroupId()))
						projects.add(cacheManager.getProject(authorization.getProjectId()));
				}
				for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
					if (authorization.getUserId().equals(user.getId()))
						projects.add(cacheManager.getProject(authorization.getProjectId()));
				}
			}
			for (ProjectFacade project: cacheManager.getProjects().values()) {
				if (project.getDefaultPrivilege() != null)
					projects.add(project);
			}
		}
		
		return projects;
	}

	private Collection<String> getChangedFiles(Project project, ObjectId oldObjectId, ObjectId newObjectId, 
			Map<String, String> gitEnvs) {
		if (gitEnvs != null && !gitEnvs.isEmpty()) {
			ListChangedFilesCommand cmd = new ListChangedFilesCommand(project.getGitDir(), gitEnvs);
			cmd.fromRev(oldObjectId.name()).toRev(newObjectId.name());
			return cmd.call();
		} else {
			return GitUtils.getChangedFiles(project.getRepository(), oldObjectId, newObjectId);
		}
	}
	
	@Sessional
	@Override
	public boolean isModificationNeedsQualityCheck(User user, Project project, String branch, @Nullable String file) {
		BranchProtection branchProtection = project.getBranchProtection(branch, user);
		if (branchProtection != null) {
			if (!ReviewRequirement.fromString(branchProtection.getReviewRequirement()).satisfied(user)) 
				return true;
			if (!branchProtection.getJobDependencies().isEmpty())
				return true;
			
			if (file != null) {
				FileProtection fileProtection = branchProtection.getFileProtection(file);
				if (fileProtection != null 
						&& !ReviewRequirement.fromString(fileProtection.getReviewRequirement()).satisfied(user)) {
					return true;
				}
			}
		}			
		return false;
	}

	@Sessional
	@Override
	public boolean isPushNeedsQualityCheck(User user, Project project, String branch, ObjectId oldObjectId, 
			ObjectId newObjectId, Map<String, String> gitEnvs) {
		BranchProtection branchProtection = project.getBranchProtection(branch, user);
		if (branchProtection != null) {
			if (!ReviewRequirement.fromString(branchProtection.getReviewRequirement()).satisfied(user)) 
				return true;

			List<Build> builds = buildManager.query(project, newObjectId.name());

			for (JobDependency dependency: branchProtection.getJobDependencies()) {
				Map<String, List<List<String>>> paramMatrix = new HashMap<>();
				for (JobParam param: dependency.getJobParams()) 
					paramMatrix.put(param.getName(), param.getValuesProvider().getValues());
				
				AtomicBoolean buildRequirementUnsatisfied = new AtomicBoolean(false);
				new MatrixRunner<List<String>>(paramMatrix) {
					
					@Override
					public void run(Map<String, List<String>> params) {
						for (Build build: builds) {
							if (build.getJobName().equals(dependency.getJobName()) && build.getParamMap().equals(params))
								return;
						}
						buildRequirementUnsatisfied.set(true);
					}
					
				}.run();
				
				if (buildRequirementUnsatisfied.get())
					return true;
			}
			
			for (String changedFile: getChangedFiles(project, oldObjectId, newObjectId, gitEnvs)) {
				FileProtection fileProtection = branchProtection.getFileProtection(changedFile);
				if (fileProtection != null  
						&& !ReviewRequirement.fromString(fileProtection.getReviewRequirement()).satisfied(user)) {
					return true;
				}
			}
		}
		return false;
	}

}
