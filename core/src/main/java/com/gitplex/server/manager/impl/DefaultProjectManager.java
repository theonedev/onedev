package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.event.ProjectRenamed;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.command.CloneCommand;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserAuthorizationManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.ProjectPrivilege;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Singleton
public class DefaultProjectManager extends AbstractEntityManager<Project> implements ProjectManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);
	
	private final ListenerRegistry listenerRegistry;
	
    private final CommitInfoManager commitInfoManager;
    
    private final UserAuthorizationManager userAuthorizationManager;
    
    private final CacheManager cacheManager;
    
    private final String gitReceiveHook;
    
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultProjectManager(Dao dao, CommitInfoManager commitInfoManager, ListenerRegistry listenerRegistry, 
    		UserAuthorizationManager userAuthorizationManager, CacheManager cacheManager) {
    	super(dao);
    	
        this.commitInfoManager = commitInfoManager;
        this.listenerRegistry = listenerRegistry;
        this.userAuthorizationManager = userAuthorizationManager;
        this.cacheManager = cacheManager;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is), "\n");
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
						repository.getConfig().setEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, 
								ConfigConstants.CONFIG_KEY_ALGORITHM, SupportedAlgorithm.HISTOGRAM);
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
    		authorization.setPrivilege(ProjectPrivilege.ADMIN);
    		authorization.setProject(project);
    		authorization.setUser(SecurityUtils.getUser());
    		userAuthorizationManager.save(authorization);
    	}
    	
    	if (oldName != null && !project.getName().equals(oldName)) {
    		listenerRegistry.post(new ProjectRenamed(project, oldName));
    	}
    	
    	dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				checkGit(project);
			}
    		
    	});
    }
    
    @Transactional
    @Override
    public void delete(Project project) {
    	Query query = getSession().createQuery("update Project set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", project);
    	query.executeUpdate();

    	dao.remove(project);
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
        
        doAfterCommit(new Runnable() {

			@Override
			public void run() {
		        commitInfoManager.cloneInfo(from, to);
			}
        	
        });
	}

	private boolean isGitHookValid(File gitDir, String hookName) {
        File hookFile = new File(gitDir, "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile);
			if (!content.contains("GITPLEX_USER_ID"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	private void checkGit(Project project) {
		File gitDir = project.getGitDir();
		if (gitDir.listFiles().length == 0) {
        	logger.info("Initializing git repository in '" + gitDir + "'...");
            try {
				Git.init().setDirectory(gitDir).setBare(true).call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		} else if (!GitUtils.isValid(gitDir)) {
        	logger.warn("Directory '" + gitDir + "' is not a valid git repository, reinitializing...");
        	FileUtils.cleanDir(gitDir);
            try {
				Git.init().setDirectory(gitDir).setBare(true).call();
			} catch (Exception e) {
				Throwables.propagate(e);
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
		for (Project project: findAll())
			checkGit(project);
	}
	
	@Transactional
	@Listen
	public void on(RefUpdated event) {
		if (event.getNewObjectId().equals(ObjectId.zeroId())) {
			Project project = event.getProject();
			String branch = GitUtils.ref2branch(event.getRefName());
			if (branch != null) {
				for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) {
					if (it.next().onBranchDelete(branch))	
						it.remove();
				}
			}
			String tag = GitUtils.ref2tag(event.getRefName());
			if (tag != null) {
				for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) {
					if (it.next().onTagDelete(tag))	
						it.remove();
				}
			}
		}
	}

}
