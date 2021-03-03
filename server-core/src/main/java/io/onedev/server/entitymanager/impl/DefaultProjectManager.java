package io.onedev.server.entitymanager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.event.ProjectCreated;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.CloneCommand;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.avatar.AvatarManager;

@Singleton
public class DefaultProjectManager extends BaseEntityManager<Project> 
		implements ProjectManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);
	
    private final CommitInfoManager commitInfoManager;
    
    private final BuildManager buildManager;
    
    private final GroupManager groupManager;
    
    private final AvatarManager avatarManager;
    
    private final SettingManager settingManager;
    
    private final SessionManager sessionManager;
    
    private final TransactionManager transactionManager;
    
    private final JobManager jobManager;
    
    private final TaskScheduler taskScheduler;
    
    private final ListenerRegistry listenerRegistry;
    
    private final RoleManager roleManager;
    
    private final UserAuthorizationManager userAuthorizationManager;
    
    private final String gitReceiveHook;
    
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
	private final Map<Long, Date> updateDates = new ConcurrentHashMap<>();
	
	private String taskId;
	
    @Inject
    public DefaultProjectManager(Dao dao, CommitInfoManager commitInfoManager,  
    		BuildManager buildManager, AvatarManager avatarManager, GroupManager groupManager,
    		SettingManager settingManager, TransactionManager transactionManager, 
    		SessionManager sessionManager, ListenerRegistry listenerRegistry, 
    		TaskScheduler taskScheduler, UserAuthorizationManager userAuthorizationManager, 
    		RoleManager roleManager, JobManager jobManager) {
    	super(dao);
    	
        this.commitInfoManager = commitInfoManager;
        this.buildManager = buildManager;
        this.groupManager = groupManager;
        this.avatarManager = avatarManager;
        this.settingManager = settingManager;
        this.transactionManager = transactionManager;
        this.sessionManager = sessionManager;
        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
        this.userAuthorizationManager = userAuthorizationManager;
        this.roleManager = roleManager;
        this.jobManager = jobManager;
        
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
    
    @Transactional
    @Override
    public void save(Project project) {
    	save(project, null);
    }
    
    @Transactional
    @Override
    public void save(Project project, String oldName) {
    	dao.persist(project);
    	if (oldName != null && !oldName.equals(project.getName())) {
        	for (JobExecutor jobExecutor: settingManager.getJobExecutors())
        		jobExecutor.onRenameProject(oldName, project.getName());
        	for (GroovyScript groovyScript: settingManager.getGroovyScripts())
        		groovyScript.onRenameProject(oldName, project.getName());
        	jobManager.schedule(project);
    	}
    }
    
    @Transactional
    @Override
    public void create(Project project) {
    	dao.persist(project);
       	checkSanity(project);
       	UserAuthorization authorization = new UserAuthorization();
       	authorization.setProject(project);
       	authorization.setUser(SecurityUtils.getUser());
       	authorization.setRole(roleManager.getOwner());
       	userAuthorizationManager.save(authorization);
       	listenerRegistry.post(new ProjectCreated(project));
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

    	for (Project fork: project.getForks()) {
    		Collection<Project> descendants = fork.getForkChildren();
    		descendants.add(fork);
    		for (Project descendant: descendants) {
            	Query<?> query = getSession().createQuery(String.format("update Issue set %s=:fork where %s=:descendant", 
            			Issue.PROP_NUMBER_SCOPE, Issue.PROP_PROJECT));
            	query.setParameter("fork", fork);
            	query.setParameter("descendant", descendant);
            	query.executeUpdate();
            	
            	query = getSession().createQuery(String.format("update Build set %s=:fork where %s=:descendant", 
            			Build.PROP_NUMBER_SCOPE, Build.PROP_PROJECT));
            	query.setParameter("fork", fork);
            	query.setParameter("descendant", descendant);
            	query.executeUpdate();
            	
            	query = getSession().createQuery(String.format("update PullRequest set %s=:fork where %s=:descendant", 
            			PullRequest.PROP_NUMBER_SCOPE, PullRequest.PROP_TARGET_PROJECT));
            	query.setParameter("fork", fork);
            	query.setParameter("descendant", descendant);
            	query.executeUpdate();
    		}
    	}
    	
    	Query<?> query = getSession().createQuery(String.format("update Project set %s=null where %s=:forkedFrom", 
    			Project.PROP_FORKED_FROM, Project.PROP_FORKED_FROM));
    	query.setParameter("forkedFrom", project);
    	query.executeUpdate();

    	query = getSession().createQuery(String.format("update PullRequest set %s=null where %s=:sourceProject", 
    			PullRequest.PROP_SOURCE_PROJECT, PullRequest.PROP_SOURCE_PROJECT));
    	query.setParameter("sourceProject", project);
    	query.executeUpdate();

    	for (Build build: project.getBuilds()) 
    		buildManager.delete(build);
    	
    	dao.remove(project);
    	
    	synchronized (repositoryCache) {
			Repository repository = repositoryCache.remove(project.getId());
			if (repository != null) 
				repository.close();
		}
    }
    
    @Sessional
    @Override
    public Project find(String projectName) {
		EntityCriteria<Project> criteria = newCriteria();
		criteria.add(Restrictions.ilike("name", projectName));
		criteria.setCacheable(true);
		return find(criteria);
    }

    @Transactional
	@Override
	public void fork(Project from, Project to) {
    	dao.persist(to);
    	
       	UserAuthorization authorization = new UserAuthorization();
       	authorization.setProject(to);
       	authorization.setUser(SecurityUtils.getUser());
       	authorization.setRole(roleManager.getOwner());
       	userAuthorizationManager.save(authorization);
    	
        FileUtils.cleanDir(to.getGitDir());
        new CloneCommand(to.getGitDir()).mirror(true).from(from.getGitDir().getAbsolutePath()).call();
        checkSanity(to);
        commitInfoManager.cloneInfo(from, to);
        avatarManager.copyAvatar(from, to);
        
        listenerRegistry.post(new ProjectCreated(to));
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
		taskScheduler.unschedule(taskId);
		synchronized(repositoryCache) {
			for (Repository repository: repositoryCache.values()) {
				repository.close();
			}
		}
	}

	@Transactional
	@Listen
	public void on(ProjectEvent event) {
		/*
		 * Update asynchronously to avoid deadlock 
		 */
		updateDates.put(event.getProject().getId(), event.getDate());
	}
	
	@Transactional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Checking projects...");
		for (Project project: query())
			checkSanity(project);
		taskId = taskScheduler.schedule(this);
	}

	@Transactional
	@Override
	public void onDeleteBranch(Project project, String branchName) {
		for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
			BranchProtection protection = it.next();
			PatternSet patternSet = PatternSet.parse(protection.getBranches());
			patternSet.getIncludes().remove(branchName);
			patternSet.getExcludes().remove(branchName);
			protection.setBranches(patternSet.toString());
			if (protection.getBranches().length() == 0)
				it.remove();
		}
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
		    		
		    	});
			}
    		
    	});
		
	}

	@Transactional
	@Override
	public void onDeleteTag(Project project, String tagName) {
		for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) { 
			TagProtection protection = it.next();
			PatternSet patternSet = PatternSet.parse(protection.getTags());
			patternSet.getIncludes().remove(tagName);
			patternSet.getExcludes().remove(tagName);
			protection.setTags(patternSet.toString());
			if (protection.getTags().length() == 0)
				it.remove();
		}
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
		    		
		    	});
			}
    		
    	});
	}
	
	@Override
	public List<Project> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
	@Sessional
	@Override
	public Collection<Project> getPermittedProjects(Permission permission) {
		Collection<Project> projects = new HashSet<>();
		
		if (SecurityUtils.isAdministrator()) {
			projects.addAll(query());
		} else {
			User user = SecurityUtils.getUser();
			if (user != null) {
				for (Membership membership: user.getMemberships()) {
					for (GroupAuthorization authorization: membership.getGroup().getAuthorizations()) {
						if (authorization.getRole().implies(permission))
							projects.add(authorization.getProject());
					}
				}
				for (UserAuthorization authorization: user.getAuthorizations()) { 
					if (authorization.getRole().implies(permission))
						projects.add(authorization.getProject());
				}
			}
			Group group = groupManager.findAnonymous();
			if (group != null) {
				if (group.isAdministrator()) {
					projects.addAll(query());
				} else {
					for (GroupAuthorization authorization: group.getAuthorizations()) { 
						if (authorization.getRole().implies(permission))
							projects.add(authorization.getProject());
					}
				}
			}
		}
		
		return projects;
	}

	private CriteriaQuery<Project> buildCriteriaQuery(Session session, EntityQuery<Project> projectQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Project> query = builder.createQuery(Project.class);
		Root<Project> root = query.from(Project.class);
		query.select(root);
		
		query.where(getPredicates(projectQuery.getCriteria(), root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: projectQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(ProjectQuery.getPath(root, Project.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(ProjectQuery.getPath(root, Project.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(ProjectQuery.getPath(root, Project.PROP_UPDATE_DATE)));
		query.orderBy(orders);
		
		return query;
	}
	
	private Predicate[] getPredicates(@Nullable io.onedev.server.search.entity.EntityCriteria<Project> criteria, 
			Root<Project> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (!SecurityUtils.isAdministrator()) {
			Collection<Long> projectIds = getPermittedProjects(new AccessProject())
					.stream().map(it->it.getId()).collect(Collectors.toSet());
			if (!projectIds.isEmpty())
				predicates.add(root.get(Project.PROP_ID).in(projectIds));
			else
				predicates.add(builder.disjunction());
		}
		if (criteria != null) 
			predicates.add(criteria.getPredicate(root, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	@Sessional
	@Override
	public List<Project> query(EntityQuery<Project> projectQuery, int firstResult, int maxResults) {
		CriteriaQuery<Project> criteriaQuery = buildCriteriaQuery(getSession(), projectQuery);
		Query<Project> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(io.onedev.server.search.entity.EntityCriteria<Project> projectCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Project> root = criteriaQuery.from(Project.class);

		criteriaQuery.where(getPredicates(projectCriteria, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public void execute() {
		try {
			transactionManager.run(new Runnable() {
	
				@Override
				public void run() {
					Date now = new Date();
					for (Iterator<Map.Entry<Long, Date>> it = updateDates.entrySet().iterator(); it.hasNext();) {
						Map.Entry<Long, Date> entry = it.next();
						if (now.getTime() - entry.getValue().getTime() > 60000) {
							Project project = get(entry.getKey());
							if (project != null)
								project.setUpdateDate(entry.getValue());
							it.remove();
						}
					}
				}
				
			});
		} catch (Exception e) {
			logger.error("Error flushing project update dates", e);
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever();
	}
	
}
