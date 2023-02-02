package io.onedev.server.entitymanager.impl;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.cluster.ProjectServer;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitTask;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.CloneCommand;
import io.onedev.server.git.command.LfsFetchAllCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.*;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.model.support.code.TagProtection;
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
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ProjectNameReservation;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.avatar.AvatarManager;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Singleton
public class DefaultProjectManager extends BaseEntityManager<Project>
		implements ProjectManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);

	private final CommitInfoManager commitInfoManager;

	private final BuildManager buildManager;

	private final AvatarManager avatarManager;

	private final SettingManager settingManager;

	private final SessionManager sessionManager;

	private final TransactionManager transactionManager;

	private final IssueManager issueManager;
	
	private final PullRequestManager pullRequestManager;

	private final LinkSpecManager linkSpecManager;

	private final JobManager jobManager;

	private final ProjectDynamicsManager dynamicsManager;

	private final ListenerRegistry listenerRegistry;

	private final RoleManager roleManager;

	private final StorageManager storageManager;

	private final UserAuthorizationManager userAuthorizationManager;

	private final ClusterManager clusterManager;

	private final GitService gitService;

	private final Collection<String> reservedNames = Sets.newHashSet("robots.txt", "sitemap.xml", "sitemap.txt",
			"favicon.ico", "favicon.png", "logo.png", "wicket", "projects");

	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();

	private volatile IMap<Long, ProjectServer> storageServers;

	private volatile ProjectCache cache;

	@Inject
	public DefaultProjectManager(Dao dao, CommitInfoManager commitInfoManager,
								 BuildManager buildManager, AvatarManager avatarManager,
								 SettingManager settingManager, TransactionManager transactionManager,
								 SessionManager sessionManager, ListenerRegistry listenerRegistry,
								 UserAuthorizationManager userAuthorizationManager, RoleManager roleManager,
								 JobManager jobManager, IssueManager issueManager, LinkSpecManager linkSpecManager,
								 StorageManager storageManager, ClusterManager clusterManager, GitService gitService,
								 ProjectDynamicsManager dynamicsManager, PullRequestManager pullRequestManager, 
								 Set<ProjectNameReservation> nameReservations) {
		super(dao);

		this.commitInfoManager = commitInfoManager;
		this.buildManager = buildManager;
		this.avatarManager = avatarManager;
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.sessionManager = sessionManager;
		this.listenerRegistry = listenerRegistry;
		this.userAuthorizationManager = userAuthorizationManager;
		this.roleManager = roleManager;
		this.jobManager = jobManager;
		this.issueManager = issueManager;
		this.linkSpecManager = linkSpecManager;
		this.storageManager = storageManager;
		this.clusterManager = clusterManager;
		this.gitService = gitService;
		this.dynamicsManager = dynamicsManager;
		this.pullRequestManager = pullRequestManager;

		for (ProjectNameReservation reservation : nameReservations)
			reservedNames.addAll(reservation.getReserved());
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ProjectManager.class);
	}

	@Override
	public Repository getRepository(Long projectId) {
		Repository repository = repositoryCache.get(projectId);
		if (repository == null) {
			synchronized (repositoryCache) {
				repository = repositoryCache.get(projectId);
				if (repository == null) {
					try {
						repository = new FileRepository(storageManager.getProjectGitDir(projectId));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					repositoryCache.put(projectId, repository);
				}
			}
		}
		return repository;
	}

	@Transactional
	@Override
	public void save(Project project) {
		String oldPath = project.getPath();
		String newPath = project.calcPath();
		if (!newPath.equals(oldPath)) {
			project.setPath(newPath);
			for (Project descendant : project.getDescendants()) {
				descendant.setPath(descendant.calcPath());
				dao.persist(descendant);
			}
		}
		dao.persist(project);
		if (oldPath != null && !oldPath.equals(project.getPath())) {
			Collection<Milestone> milestones = new ArrayList<>();
			for (Milestone milestone : issueManager.queryUsedMilestones(project)) {
				if (!project.isSelfOrAncestorOf(milestone.getProject())
						&& !milestone.getProject().isSelfOrAncestorOf(project)) {
					milestones.add(milestone);
				}
			}
			issueManager.clearSchedules(project, milestones);
			settingManager.onMoveProject(oldPath, project.getPath());

			for (LinkSpec link : linkSpecManager.query()) {
				for (IssueQueryUpdater updater : link.getQueryUpdaters())
					updater.onMoveProject(oldPath, project.getPath());
			}

			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					scheduleTree(project);
				}

			});
		}
	}

	private void scheduleTree(Project project) {
		Long projectId = project.getId();
		submitToProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				sessionManager.run(new Runnable() {

					@Override
					public void run() {
						jobManager.schedule(load(projectId));
					}

				});
				return null;
			}

		});
		for (Project child : project.getChildren())
			scheduleTree(child);
	}

	@Transactional
	@Override
	public void create(Project project) {
		Project parent = project.getParent();
		if (parent != null && parent.isNew())
			create(parent);
		project.setPath(project.calcPath());

		ProjectDynamics update = new ProjectDynamics();
		project.setDynamics(update);
		dynamicsManager.save(update);
		dao.persist(project);

		var projectDir = storageManager.getProjectDir(project.getId());
		FileUtils.cleanDir(projectDir);
		checkGitDir(project.getId());
		
		var gitDir = storageManager.getProjectGitDir(project.getId());
		HookUtils.checkHooks(gitDir);
		checkGitConfig(project.getId(), project.getGitPackConfig());

		UserAuthorization authorization = new UserAuthorization();
		authorization.setProject(project);
		authorization.setUser(SecurityUtils.getUser());
		authorization.setRole(roleManager.getOwner());
		userAuthorizationManager.save(authorization);

		updateStorageServer(project);
		listenerRegistry.post(new ProjectCreated(project));
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			Long projectId = project.getId();

			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					cache.remove(projectId);
					ProjectServer server = storageServers.remove(projectId);
					if (server != null) {
						clusterManager.submitToServer(server.getPrimary(), new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								jobManager.unschedule(projectId);
								return null;
							}

						});
					}
				}

			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Project) {
			ProjectFacade facade = ((Project) event.getEntity()).getFacade();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					cache.put(facade.getId(), facade);
				}

			});
		}
	}

	@Transactional
	@Override
	public void delete(Project project) {
		for (Project child : project.getChildren())
			delete(child);

		Usage usage = new Usage();
		usage.add(settingManager.onDeleteProject(project.getPath()));

		for (LinkSpec link : linkSpecManager.query()) {
			for (IssueQueryUpdater updater : link.getQueryUpdaters())
				usage.add(updater.onDeleteProject(project.getPath()).prefix("issue setting").prefix("administration"));
		}

		usage.checkInUse("Project '" + project.getPath() + "'");

		for (Project fork : project.getForks()) {
			Collection<Project> forkChildren = fork.getForkChildren();
			forkChildren.add(fork);
			for (Project forkChild : forkChildren) {
				Query<?> query = getSession().createQuery(String.format("update Issue set %s=:fork where %s=:descendant",
						Issue.PROP_NUMBER_SCOPE, Issue.PROP_PROJECT));
				query.setParameter("fork", fork);
				query.setParameter("descendant", forkChild);
				query.executeUpdate();

				query = getSession().createQuery(String.format("update Build set %s=:fork where %s=:descendant",
						Build.PROP_NUMBER_SCOPE, Build.PROP_PROJECT));
				query.setParameter("fork", fork);
				query.setParameter("descendant", forkChild);
				query.executeUpdate();

				query = getSession().createQuery(String.format("update PullRequest set %s=:fork where %s=:descendant",
						PullRequest.PROP_NUMBER_SCOPE, PullRequest.PROP_TARGET_PROJECT));
				query.setParameter("fork", fork);
				query.setParameter("descendant", forkChild);
				query.executeUpdate();
			}
		}

		Query<?> query = getSession().createQuery(String.format("update Project set %s=null where %s=:forkedFrom",
				Project.PROP_FORKED_FROM, Project.PROP_FORKED_FROM));
		query.setParameter("forkedFrom", project);
		query.executeUpdate();

		for (PullRequest request: project.getOutgoingRequests()) {
			if (!request.getTargetProject().equals(project) && request.isOpen())
				pullRequestManager.discard(request, "Source project is deleted.");
		}
		
		query = getSession().createQuery(String.format("update PullRequest set %s=null where %s=:sourceProject",
				PullRequest.PROP_SOURCE_PROJECT, PullRequest.PROP_SOURCE_PROJECT));
		query.setParameter("sourceProject", project);
		query.executeUpdate();

		for (Build build : project.getBuilds())
			buildManager.delete(build);

		dao.remove(project);
		dynamicsManager.delete(project.getDynamics());

		synchronized (repositoryCache) {
			Repository repository = repositoryCache.remove(project.getId());
			if (repository != null)
				repository.close();
		}
		
		listenerRegistry.post(new ProjectDeleted(SecurityUtils.getUser(), new Date(), project));
	}

	@Override
	public Project findByPath(String path) {
		ProjectFacade project = cache.find(path);
		if (project != null)
			return load(project.getId());
		else
			return null;
	}

	@Sessional
	@Override
	public Project findByServiceDeskName(String serviceDeskName) {
		Long projectId = null;
		for (ProjectFacade facade : cache.values()) {
			if (serviceDeskName.equals(facade.getServiceDeskName())) {
				projectId = facade.getId();
				break;
			}
		}
		if (projectId != null)
			return load(projectId);
		else
			return null;
	}

	@Sessional
	@Override
	public Project setup(String path) {
		List<String> names = Splitter.on("/").omitEmptyStrings().trimResults().splitToList(path);
		Project project = null;
		for (String name : names) {
			Project child;
			if (project == null || !project.isNew()) {
				// Query database directly instead of calling findByName to fix issue 
				// #923 - Multi level projects after import and 1dev upgrade are mingled
				EntityCriteria<Project> criteria = EntityCriteria.of(Project.class);
				if (project != null)
					criteria.add(Restrictions.eq(Project.PROP_PARENT, project));
				else
					criteria.add(Restrictions.isNull(Project.PROP_PARENT));
				criteria.add(Restrictions.eq(Project.PROP_NAME, name));
				child = find(criteria);
				if (child == null) {
					if (project == null && !SecurityUtils.canCreateRootProjects())
						throw new UnauthorizedException("Not authorized to create root project");
					if (project != null && !SecurityUtils.canCreateChildren(project))
						throw new UnauthorizedException("Not authorized to create project under '" + project.getPath() + "'");
					child = new Project();
					child.setName(name);
					child.setParent(project);
				}
			} else {
				child = new Project();
				child.setName(name);
				child.setParent(project);
			}
			project = child;
		}

		Project parent = project.getParent();
		while (parent != null && parent.isNew()) {
			parent.setCodeManagement(false);
			parent.setIssueManagement(false);
			parent = parent.getParent();
		}
		project.setPath(path);
		return project;
	}

	@Sessional
	@Override
	public Project find(Project parent, String name) {
		Long projectId = null;
		for (ProjectFacade facade : cache.values()) {
			if (facade.getName().equalsIgnoreCase(name)
					&& Objects.equals(Project.idOf(parent), facade.getParentId())) {
				projectId = facade.getId();
				break;
			}
		}
		if (projectId != null)
			return load(projectId);
		else
			return null;
	}

	@Transactional
	@Override
	public void fork(Project from, Project to) {
		to.getDynamics().setLastCommitDate(from.getDynamics().getLastCommitDate());
		Long fromId = from.getId();
		String fromPath = from.getPath();
		Long toId = to.getId();

		GitPackConfig gitPackConfig = to.getGitPackConfig();
		runOnProjectServer(toId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				File toGitDir = storageManager.getProjectGitDir(toId);
				FileUtils.cleanDir(toGitDir);

				UUID fromStorageServerUUID = getStorageServerUUID(fromId, true);
				if (fromStorageServerUUID.equals(clusterManager.getLocalServerUUID())) {
					File fromGitDir = storageManager.getProjectGitDir(fromId);
					new CloneCommand(toGitDir, fromGitDir.getAbsolutePath()).noLfs(true).mirror(true).run();
					storageManager.initLfsDir(toId);
					new LfsFetchAllCommand(toGitDir).run();
				} else {
					CommandUtils.callWithClusterCredential(new GitTask<Void>() {

						@Override
						public Void call(Commandline git) {
							String remoteUrl = clusterManager.getServerUrl(fromStorageServerUUID) + "/" + fromPath;
							new CloneCommand(toGitDir, remoteUrl) {

								@Override
								protected Commandline newGit() {
									return git;
								}

							}.noLfs(true).mirror(true).run();
							return null;
						}

					});

					storageManager.initLfsDir(toId);

					CommandUtils.callWithClusterCredential(new GitTask<Void>() {

						@Override
						public Void call(Commandline git) {
							new LfsFetchAllCommand(toGitDir) {

								@Override
								protected Commandline newGit() {
									return git;
								}

							}.run();
							return null;
						}

					});

				}

				HookUtils.checkHooks(toGitDir);
				checkGitConfig(toId, gitPackConfig);

				commitInfoManager.cloneInfo(fromId, toId);
				avatarManager.copyProjectAvatar(fromId, toId);
				return null;
			}

		});
	}

	private void updateStorageServer(Project project) {
		ProjectServer storageServer = new ProjectServer(
				clusterManager.getLocalServerUUID(), Lists.newArrayList());
		storageServers.put(project.getId(), storageServer);
	}

	@Transactional
	@Override
	public void clone(Project project, String repositoryUrl) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				File gitDir = storageManager.getProjectGitDir(projectId);
				FileUtils.cleanDir(gitDir);
				new CloneCommand(gitDir, repositoryUrl).mirror(true).noLfs(true).run();
				storageManager.initLfsDir(projectId);
				new LfsFetchAllCommand(gitDir).run();
				return null;
			}

		});

		List<ImmutableTriple<String, ObjectId, ObjectId>> refUpdatedEventData = new ArrayList<>();

		for (RefFacade ref : project.getBranchRefs()) {
			refUpdatedEventData.add(new ImmutableTriple<>(ref.getName(),
					ObjectId.zeroId(), ref.getObjectId()));
		}
		for (RefFacade ref : project.getTagRefs()) {
			refUpdatedEventData.add(new ImmutableTriple<>(ref.getName(),
					ObjectId.zeroId(), ref.getPeeledObj().getId().copy()));
		}

		sessionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				try {
					Project project = load(projectId);

					for (ImmutableTriple<String, ObjectId, ObjectId> each : refUpdatedEventData) {
						String refName = each.getLeft();
						ObjectId oldObjectId = each.getMiddle();
						ObjectId newObjectId = each.getRight();
						if (!newObjectId.equals(ObjectId.zeroId()))
							project.cacheObjectId(refName, newObjectId);
						else
							project.cacheObjectId(refName, null);

						listenerRegistry.post(new RefUpdated(project, refName, oldObjectId, newObjectId));
					}
				} catch (Exception e) {
					logger.error("Error posting ref updated event", e);
				}
			}

		});

	}

	private void checkGitDir(Long projectId) {
		File gitDir = storageManager.getProjectGitDir(projectId);
		if (gitDir.listFiles().length == 0) {
			logger.info("Initializing git repository in '" + gitDir + "'...");
			try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			storageManager.initLfsDir(projectId);
		} else if (!GitUtils.isValid(gitDir)) {
			logger.warn("Directory '" + gitDir + "' is not a valid git repository, reinitializing...");
			FileUtils.cleanDir(gitDir);
			storageManager.initLfsDir(projectId);
			try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			storageManager.initLfsDir(projectId);
		}
	}

	private boolean updateStoredConfig(StoredConfig storedConfig, String section,
									   String name, @Nullable String value) {
		if (value != null) {
			if (!value.equals(storedConfig.getString(section, "null", name))) {
				storedConfig.setString(section, null, name, value);
				return true;
			}
		} else {
			if (storedConfig.getString(section, null, name) != null) {
				storedConfig.unset(section, null, name);
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkGitConfig(Long projectId, GitPackConfig gitPackConfig) {
		try {
			StoredConfig storedConfig = getRepository(projectId).getConfig();
			boolean changed = false;
			if (storedConfig.getEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, ConfigConstants.CONFIG_KEY_ALGORITHM,
					SupportedAlgorithm.MYERS) != SupportedAlgorithm.HISTOGRAM) {
				storedConfig.setEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, ConfigConstants.CONFIG_KEY_ALGORITHM,
						SupportedAlgorithm.HISTOGRAM);
				changed = true;
			}
			if (!storedConfig.getBoolean("uploadpack", "allowAnySHA1InWant", false)) {
				storedConfig.setBoolean("uploadpack", null, "allowAnySHA1InWant", true);
				changed = true;
			}

			var packSection = "pack";
			if (updateStoredConfig(storedConfig, packSection, "windowMemory",
					gitPackConfig.getWindowMemory())) {
				changed = true;
			}
			if (updateStoredConfig(storedConfig, packSection, "packSizeLimit",
					gitPackConfig.getPackSizeLimit())) {
				changed = true;
			}
			if (updateStoredConfig(storedConfig, packSection, "threads",
					gitPackConfig.getThreads())) {
				changed = true;
			}
			if (updateStoredConfig(storedConfig, packSection, "window",
					gitPackConfig.getWindow())) {
				changed = true;
			}

			if (changed)
				storedConfig.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized (repositoryCache) {
			for (Repository repository : repositoryCache.values()) {
				repository.close();
			}
		}
	}

	@Transactional
	@Listen(1)
	public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		cache = new ProjectCache(hazelcastInstance.getReplicatedMap("projectCache"));

		Map<Long, Project> projects = new HashMap<>();
		for (Project project : query()) {
			String path = project.getPath();
			if (!path.equals(project.calcPath()))
				project.setPath(path);
			cache.put(project.getId(), project.getFacade());
			projects.put(project.getId(), project);
		}

		Map<Long, ProjectDynamics> updates = new HashMap<>();
		for (ProjectDynamics update : dynamicsManager.query())
			updates.put(update.getId(), update);

		logger.info("Checking projects...");

		storageServers = hazelcastInstance.getMap("projectStorageServers");

		UUID localServerUUID = clusterManager.getLocalServerUUID();
		for (var file : storageManager.getProjectsDir().listFiles()) {
			var projectId = Long.valueOf(file.getName());
			var project = projects.get(projectId);
			if (project != null) {
				checkGitDir(projectId);

				if (project.isCodeManagement()) {
					ProjectDynamics update = updates.get(project.getDynamics().getId());
					if (update.getLastCommitDate() == null) {
						RevCommit lastCommit = GitUtils.getLastCommit(getRepository(projectId));
						if (lastCommit != null)
							update.setLastCommitDate(lastCommit.getCommitterIdent().getWhen());
					}
				}
				HookUtils.checkHooks(storageManager.getProjectGitDir(projectId));
				checkGitConfig(projectId, project.getGitPackConfig());
				storageServers.put(projectId, new ProjectServer(localServerUUID, Lists.newArrayList()));
			}
		}

		hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterManager.isLeaderServer()) {
					UUID serverUUID = membershipEvent.getMember().getUuid();
					Set<Long> projectsToRemove = new HashSet<>();
					for (Map.Entry<Long, ProjectServer> entry : storageServers.entrySet()) {
						ProjectServer server = entry.getValue();
						if (server.getBackups().contains(serverUUID)) {
							List<UUID> backups = new ArrayList<>(server.getBackups());
							backups.remove(serverUUID);
							entry.setValue(new ProjectServer(server.getPrimary(), backups));
						} else if (server.getPrimary().equals(serverUUID)) {
							if (server.getBackups().isEmpty()) {
								projectsToRemove.add(entry.getKey());
							} else {
								List<UUID> backups = new ArrayList<>(server.getBackups());
								entry.setValue(new ProjectServer(backups.remove(0), backups));
							}
						}
					}

					for (Long projectId : projectsToRemove)
						storageServers.remove(projectId);
				}
			}

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
			}

		});
	}

	@Transactional
	@Override
	public void onDeleteBranch(Project project, String branchName) {
		for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext(); ) {
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
		gitService.deleteBranch(project, branchName);
	}

	@Transactional
	@Override
	public void onDeleteTag(Project project, String tagName) {
		for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext(); ) {
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
		gitService.deleteTag(project, tagName);
	}

	@Override
	public List<Project> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}

	private void addSubTreeIds(Collection<Long> projectIds, Project project) {
		projectIds.add(project.getId());
		for (Project descendant : project.getDescendants())
			projectIds.add(descendant.getId());
	}

	@Override
	public Collection<Project> getPermittedProjects(Permission permission) {
		ProjectCache cacheClone = cache.clone();

		Collection<Long> permittedProjectIds;
		User user = SecurityUtils.getUser();
		if (user != null) {
			if (user.isRoot() || user.isSystem()) {
				return cacheClone.getProjects();
			} else {
				permittedProjectIds = new HashSet<>();
				for (Group group : user.getGroups()) {
					if (group.isAdministrator())
						return cacheClone.getProjects();
					for (GroupAuthorization authorization : group.getAuthorizations()) {
						if (authorization.getRole().implies(permission))
							addSubTreeIds(permittedProjectIds, authorization.getProject());
					}
				}
				Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
				if (defaultLoginGroup != null) {
					if (defaultLoginGroup.isAdministrator())
						return cacheClone.getProjects();
					for (GroupAuthorization authorization : defaultLoginGroup.getAuthorizations()) {
						if (authorization.getRole().implies(permission))
							addSubTreeIds(permittedProjectIds, authorization.getProject());
					}
				}

				for (UserAuthorization authorization : user.getProjectAuthorizations()) {
					if (authorization.getRole().implies(permission))
						addSubTreeIds(permittedProjectIds, authorization.getProject());
				}
				addIdsPermittedByDefaultRole(cacheClone, permittedProjectIds, permission);
			}
		} else {
			permittedProjectIds = new HashSet<>();
			if (settingManager.getSecuritySetting().isEnableAnonymousAccess())
				addIdsPermittedByDefaultRole(cacheClone, permittedProjectIds, permission);
		}

		return permittedProjectIds.stream().map(it -> load(it)).collect(Collectors.toSet());
	}

	private void addIdsPermittedByDefaultRole(ProjectCache cache, Collection<Long> projectIds,
											  Permission permission) {
		for (ProjectFacade project : cache.values()) {
			if (project.getDefaultRoleId() != null) {
				Role defaultRole = roleManager.load(project.getDefaultRoleId());
				if (defaultRole.implies(permission))
					projectIds.addAll(cache.getSubtreeIds(project.getId()));
			}
		}
	}

	private CriteriaQuery<Project> buildCriteriaQuery(Session session, EntityQuery<Project> projectQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Project> query = builder.createQuery(Project.class);
		Root<Project> root = query.from(Project.class);
		query.select(root);

		query.where(getPredicates(projectQuery.getCriteria(), query, root, builder));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort : projectQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(ProjectQuery.getPath(root, Project.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(ProjectQuery.getPath(root, Project.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.asc(ProjectQuery.getPath(root, Project.PROP_PATH)));
		query.orderBy(orders);

		return query;
	}

	private Predicate[] getPredicates(@Nullable Criteria<Project> criteria, CriteriaQuery<?> query,
									  From<Project, Project> from, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = getPermittedProjects(new AccessProject());
			if (!projects.isEmpty()) {
				predicates.add(Criteria.forManyValues(builder, from.get(Project.PROP_ID),
						projects.stream().map(it -> it.getId()).collect(Collectors.toSet()), getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}
		if (criteria != null)
			predicates.add(criteria.getPredicate(query, from, builder));
		return predicates.toArray(new Predicate[0]);
	}

	@Sessional
	@Override
	public List<Project> query(EntityQuery<Project> query, int firstResult, int maxResults) {
		CriteriaQuery<Project> criteriaQuery = buildCriteriaQuery(getSession(), query);
		Query<Project> projectQuery = getSession().createQuery(criteriaQuery);
		projectQuery.setFirstResult(firstResult);
		projectQuery.setMaxResults(maxResults);
		return projectQuery.getResultList();
	}

	@Sessional
	@Override
	public int count(Criteria<Project> projectCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Project> root = criteriaQuery.from(Project.class);

		criteriaQuery.where(getPredicates(projectCriteria, criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public Collection<Long> getSubtreeIds(Long projectId) {
		return cache.getSubtreeIds(projectId);
	}

	@Override
	public Collection<Long> getIds() {
		return new HashSet<>(cache.keySet());
	}

	@Override
	public Predicate getPathMatchPredicate(CriteriaBuilder builder, Path<Project> path, String pathPattern) {
		return Criteria.forManyValues(builder, path.get(Project.PROP_ID),
				cache.getMatchingIds(pathPattern), cache.keySet());
	}

	@Transactional
	@Override
	public void move(Collection<Project> projects, Project parent) {
		for (Project project : projects) {
			project.setParent(parent);
			save(project);
		}
	}

	@Transactional
	@Override
	public void delete(Collection<Project> projects) {
		Collection<Project> independents = new HashSet<>(projects);
		for (Iterator<Project> it = independents.iterator(); it.hasNext(); ) {
			Project independent = it.next();
			for (Project each : independents) {
				if (!each.equals(independent) && each.isSelfOrAncestorOf(independent)) {
					it.remove();
					break;
				}
			}
		}
		for (Project independent : independents)
			delete(independent);
	}

	@Override
	public List<ProjectFacade> getChildren(Long projectId) {
		return cache.getChildren(projectId);
	}

	@Override
	public ProjectCache cloneCache() {
		return cache.clone();
	}

	@Override
	public String getFavoriteQuery() {
		User user = SecurityUtils.getUser();
		if (user != null && !user.getProjectQueryPersonalization().getQueries().isEmpty()) {
			return user.getProjectQueryPersonalization().getQueries().iterator().next().getQuery();
		} else {
			GlobalProjectSetting projectSetting = settingManager.getProjectSetting();
			if (!projectSetting.getNamedQueries().isEmpty())
				return projectSetting.getNamedQueries().iterator().next().getQuery();
		}
		return null;
	}

	@Override
	public UUID getStorageServerUUID(Long projectId, boolean mustExist) {
		ProjectServer server = storageServers.get(projectId);
		if (server != null)
			return server.getPrimary();
		else if (mustExist)
			throw new ExplicitException("Storage not found for project id: " + projectId);
		else
			return null;
	}

	@Override
	public Map<Long, ProjectServer> getStorageServers() {
		return storageServers;
	}

	@Override
	public <T> T runOnProjectServer(Long projectId, ClusterTask<T> task) {
		return clusterManager.runOnServer(getStorageServerUUID(projectId, true), task);
	}

	@Override
	public <T> Future<T> submitToProjectServer(Long projectId, ClusterTask<T> task) {
		return clusterManager.submitToServer(getStorageServerUUID(projectId, true), task);
	}

	@Override
	public ProjectFacade findFacadeByPath(String path) {
		return cache.find(path);
	}

	@Override
	public ProjectFacade findFacadeById(Long id) {
		return cache.get(id);
	}

	@Override
	public File getLfsObjectsDir(Long projectId) {
		return new File(storageManager.getProjectGitDir(projectId), "lfs/objects");
	}

	@Override
	public Collection<String> getReservedNames() {
		return reservedNames;
	}

	@Nullable
	@Override
	public ArtifactInfo getSiteArtifactInfo(Long projectId, String siteArtifactPath) {
		return runOnProjectServer(projectId, new ClusterTask<ArtifactInfo>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ArtifactInfo call() throws Exception {
				return LockUtils.read(Project.getSiteLockName(projectId), new Callable<ArtifactInfo>() {
					
					@Override
					public ArtifactInfo call() throws Exception {
						File siteArtifact = new File(storageManager.getProjectSiteDir(projectId), siteArtifactPath);
						if (siteArtifact.exists()) {
							if (siteArtifact.isFile()) {
								String mediaType = Files.probeContentType(siteArtifact.toPath());
								if (mediaType == null)
									mediaType = MediaType.APPLICATION_OCTET_STREAM;
								return new FileInfo(siteArtifactPath, siteArtifact.length(), siteArtifact.lastModified(), mediaType);
							} else {
								return new DirectoryInfo(siteArtifactPath, siteArtifact.lastModified(), null);
							}
						} else {
							return null;
						}
					}

				});
			}

		});
	}

}
