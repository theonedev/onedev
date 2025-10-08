package io.onedev.server.service.impl;

import static io.onedev.commons.utils.FileUtils.cleanDir;
import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.k8shelper.KubernetesHelper.BEARER;
import static io.onedev.server.git.CommandUtils.callWithClusterCredential;
import static io.onedev.server.git.GitUtils.getDefaultBranch;
import static io.onedev.server.git.GitUtils.getReachableCommits;
import static io.onedev.server.git.GitUtils.isValid;
import static io.onedev.server.git.GitUtils.setDefaultBranch;
import static io.onedev.server.model.Project.ATTACHMENT_DIR;
import static io.onedev.server.model.Project.SHARE_TEST_DIR;
import static io.onedev.server.model.Project.SITE_DIR;
import static io.onedev.server.model.Project.SORT_FIELDS;
import static io.onedev.server.model.Project.getSiteLockName;
import static io.onedev.server.replica.ProjectReplica.Type.BACKUP;
import static io.onedev.server.replica.ProjectReplica.Type.PRIMARY;
import static io.onedev.server.replica.ProjectReplica.Type.REDUNDANT;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.util.DirectoryVersionUtils.FILE_VERSION;
import static io.onedev.server.util.DirectoryVersionUtils.increaseVersion;
import static io.onedev.server.util.DirectoryVersionUtils.isVersionFile;
import static io.onedev.server.util.DirectoryVersionUtils.readVersion;
import static io.onedev.server.util.DirectoryVersionUtils.writeVersion;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static io.onedev.server.util.criteria.Criteria.forManyValues;
import static io.onedev.server.web.translation.Translation._T;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.eclipse.jgit.lib.Constants.R_HEADS;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.StorageService;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.cluster.ConnectionEvent;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.ActiveServerChanged;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.ProjectCreated;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopped;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitTask;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.CloneCommand;
import io.onedev.server.git.command.LfsFetchAllCommand;
import io.onedev.server.git.command.LfsFetchCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Iteration;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.administration.GlobalProjectSetting;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.replica.ProjectReplica;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.issue.IssueQueryUpdater;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectLabelService;
import io.onedev.server.service.ProjectLastEventDateService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.service.UserService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.ProjectNameReservation;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.util.concurrent.BatchWorkExecutionService;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.ProjectCache;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.avatar.AvatarService;
import io.onedev.server.xodus.CommitInfoService;
import io.onedev.server.xodus.VisitInfoService;

@Singleton
public class DefaultProjectService extends BaseEntityService<Project>
		implements ProjectService, Serializable, SchedulableTask {

	private static final String DELETE_MARK = "to-be-deleted-when-onedev-is-restarted";

	private static final String LFS_SINCE_COMMITS = "lfs/.lfs-since-commits";
	
	private static final int SYNC_PRIORITY = 20;
	
	private static final int HOUSE_KEEPING_PRIORITY = 30;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectService.class);

	@Inject
	private CommitInfoService commitInfoService;

	@Inject
	private BuildService buildService;

	@Inject
	private AvatarService avatarService;

	@Inject
	private SettingService settingService;

	@Inject
	private SessionService sessionService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private IssueService issueService;

	@Inject
	private PullRequestService pullRequestService;

	@Inject
	private LinkSpecService linkSpecService;

	@Inject
	private ProjectLastEventDateService lastEventDateService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Inject
	private RoleService roleService;

	@Inject
	private UserAuthorizationService userAuthorizationService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private UserService userService;	

	@Inject
	private BatchWorkExecutionService batchWorkExecutionService;

	@Inject
	private AttachmentService attachmentService;

	@Inject
	private GitService gitService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private VisitInfoService visitInfoService;

	@Inject
	private StorageService storageService;

	@Inject
	private PackService packService;

	@Inject
	private PackBlobService packBlobService;

	@Inject
	private ProjectLabelService labelService;

	@Inject
	private AuditService auditService;

	private final Collection<String> reservedNames = Sets.newHashSet("robots.txt", "sitemap.xml", "sitemap.txt",
			"favicon.ico", "favicon.png", "logo.png", "wicket", "projects");

	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
	private volatile IMap<Long, LinkedHashMap<String, ProjectReplica>> replicas;
	
	private volatile IMap<Long, String> activeServers;
	
	private volatile ProjectCache cache;
	
	private volatile String taskId;

	@Inject
	public DefaultProjectService(Set<ProjectNameReservation> nameReservations) {
		for (ProjectNameReservation reservation : nameReservations)
			reservedNames.addAll(reservation.getReserved());
	}

	@Override
	public Repository getRepository(Long projectId) {
		Repository repository = repositoryCache.get(projectId);
		if (repository == null) {
			synchronized (repositoryCache) {
				repository = repositoryCache.get(projectId);
				if (repository == null) {
					try {
						repository = new FileRepository(getGitDir(projectId));
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
	public void update(Project project) {
		Preconditions.checkState(!project.isNew());
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
			Collection<Iteration> iterations = new ArrayList<>();
			for (Iteration iteration : issueService.queryUsedIterations(project)) {
				if (!project.isSelfOrAncestorOf(iteration.getProject())
						&& !iteration.getProject().isSelfOrAncestorOf(project)) {
					iterations.add(iteration);
				}
			}
			issueService.clearSchedules(project, iterations);
			settingService.onMoveProject(oldPath, project.getPath());

			for (LinkSpec link : linkSpecService.query()) {
				for (IssueQueryUpdater updater : link.getQueryUpdaters())
					updater.onMoveProject(oldPath, project.getPath());
			}
		}
	}

	@Transactional
	@Override
	public void create(User user, Project project) {
		Preconditions.checkState(project.isNew());
		Project parent = project.getParent();
		if (parent != null && parent.isNew()) {
			create(user, parent);
			auditService.audit(parent, "created project", null, VersionedXmlDoc.fromBean(parent).toXML());
		}
		project.setPath(project.calcPath());

		ProjectLastActivityDate lastEventDate = new ProjectLastActivityDate();
		project.setLastActivityDate(lastEventDate);
		lastEventDateService.create(lastEventDate);
		dao.persist(project);

		UserAuthorization authorization = new UserAuthorization();
		authorization.setProject(project);
		authorization.setUser(user);
		authorization.setRole(roleService.getOwner());
		userAuthorizationService.createOrUpdate(authorization);
		
		Long projectId = project.getId();
		LinkedHashMap<String, ProjectReplica> replicasOfProject = clusterService.addProject(
				new HashMap<>(replicas), projectId);
		var gitPackConfig = project.getGitPackConfig();
		for (var entry: replicasOfProject.entrySet()) {
			var replica = entry.getValue();
			clusterService.runOnServer(entry.getKey(), () -> {
				var projectDir = getProjectDir(projectId);
				cleanDir(projectDir);
				replica.saveType(projectDir);
				initGit(projectId, gitPackConfig);
				return null;
			});
		}
		replicas.put(projectId, replicasOfProject);
		var activeServer = replicasOfProject.entrySet().stream()
				.filter(it -> it.getValue().getType() == PRIMARY)
				.findFirst()
				.get().getKey();
		activeServers.put(projectId, activeServer);
		listenerRegistry.post(new ProjectCreated(project));
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Project) {
			ProjectFacade facade = ((Project) event.getEntity()).getFacade();
			transactionService.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionService.runAfterCommit(() -> {
				cache.remove(projectId);
				activeServers.remove(projectId);
				var replicasOfProject = replicas.remove(projectId);
				if (replicasOfProject != null) {
					for (var server: replicasOfProject.keySet()) {
						clusterService.submitToServer(server, () -> {
							markStorageForDelete(projectId);
							return null;
						});
					}
				}
			});
		}
	}

	@Transactional
	@Override
	public void delete(Collection<Project> projects) {
		for (Project independent : Project.getIndependents(projects))
			delete(independent);
	}

	@Transactional
	@Override
	public void delete(Project project) {
		for (Project child : project.getChildren())
			delete(child);

		Usage usage = new Usage();
		usage.add(settingService.onDeleteProject(project.getPath()));

		for (LinkSpec link : linkSpecService.query()) {
			for (IssueQueryUpdater updater : link.getQueryUpdaters())
				usage.add(updater.onDeleteProject(project.getPath()).prefix("issue setting").prefix("administration"));
		}

		usage.checkInUse("Project '" + project.getPath() + "'");
		
		for (Project fork : project.getForks()) {
			Collection<Project> forkChildren = fork.getForkDescendants();
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

		var user = userService.getSystem();
		for (PullRequest request: project.getOutgoingRequests()) {
			if (!request.getTargetProject().equals(project) && request.isOpen())
				pullRequestService.discard(user, request, "Source project is deleted.");
		}

		query = getSession().createQuery(String.format("update PullRequest set %s=null where %s=:sourceProject",
				PullRequest.PROP_SOURCE_PROJECT, PullRequest.PROP_SOURCE_PROJECT));
		query.setParameter("sourceProject", project);
		query.executeUpdate();

		for (Build build : project.getBuilds())
			buildService.delete(build);

		for (Pack pack : project.getPacks())
			packService.delete(pack);

		for (PackBlob packBlob : project.getPackBlobs())
			packBlobService.delete(packBlob);

		dao.remove(project);
		lastEventDateService.delete(project.getLastActivityDate());

		synchronized (repositoryCache) {
			Repository repository = repositoryCache.remove(project.getId());
			if (repository != null)
				repository.close();
		}

		listenerRegistry.post(new ProjectDeleted(project));
	}

	@Override
	public Project findByPath(String path) {
		var facade = findFacadeByPath(path);
		return facade!=null? load(facade.getId()): null;
	}

	@Override
	public Project findByKey(String key) {
		var facade = findFacadeByKey(key);
		return facade!=null? load(facade.getId()): null;
	}
	
	@Sessional
	@Override
	public Project findByServiceDeskEmailAddress(String serviceDeskEmailAddress) {
		Long projectId = null;
		for (ProjectFacade facade : cache.values()) {
			if (serviceDeskEmailAddress.equalsIgnoreCase(facade.getServiceDeskEmailAddress())) {
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
	public Project setup(Subject subject, String path) {
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
					if (project == null && !SecurityUtils.canCreateRootProjects(subject))
						throw new UnauthorizedException(_T("Not authorized to create root project"));
					if (project != null && !SecurityUtils.canCreateChildren(subject, project))
						throw new UnauthorizedException(MessageFormat.format(_T("Not authorized to create project under \"{0}\""), project.getPath()));
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
		Long fromId = from.getId();
		String fromPath = from.getPath();
		Long toId = to.getId();
		var withLfs = hasLfsObjects(fromId);

		GitPackConfig gitPackConfig = to.getGitPackConfig();
		runOnActiveServer(toId, () -> {
			File toGitDir = getGitDir(toId);
			cleanDir(toGitDir);

			String fromActiveServer = getActiveServer(fromId, true);
			if (fromActiveServer.equals(clusterService.getLocalServerAddress())) {
				File fromGitDir = getGitDir(fromId);
				new CloneCommand(toGitDir, fromGitDir.getAbsolutePath()).noLfs(true).mirror(true).run();
				storageService.initLfsDir(toId);
				if (withLfs)
					// Use origin instead of real url as otherwise the command will 
					// report EOF error when fetch from a local path
					new LfsFetchAllCommand(toGitDir, "origin").run();
			} else {
				var remoteUrl = clusterService.getServerUrl(fromActiveServer) + "/" + fromPath;
				callWithClusterCredential(git -> {
					new CloneCommand(toGitDir, remoteUrl) {

						@Override
						protected Commandline newGit() {
							return git;
						}

					}.noLfs(true).mirror(true).run();
					return null;
				});

				storageService.initLfsDir(toId);

				if (withLfs) {
					callWithClusterCredential(git -> {
						new LfsFetchAllCommand(toGitDir, remoteUrl) {

							@Override
							protected Commandline newGit() {
								return git;
							}

						}.run();
						return null;
					});
				}
			}

			HookUtils.checkHooks(toGitDir);
			checkGitConfig(toId, gitPackConfig);
			commitInfoService.cloneInfo(fromId, toId);
			avatarService.copyProjectAvatar(fromId, toId);
			return null;
		});
		
		postBranchUpdatedEvents(toId);		
	}

	@Transactional
	@Override
	public void clone(Project project, String repositoryUrl) {
		Long projectId = project.getId();
		runOnActiveServer(projectId, () -> {
			File gitDir = getGitDir(projectId);
			cleanDir(gitDir);
			new CloneCommand(gitDir, repositoryUrl).mirror(true).noLfs(true).run();
			storageService.initLfsDir(projectId);
			new LfsFetchAllCommand(gitDir, repositoryUrl).run();
			HookUtils.checkHooks(gitDir);
			checkGitConfig(projectId, project.getGitPackConfig());
			return null;
		});
		postBranchUpdatedEvents(projectId);
	}

	private void postBranchUpdatedEvents(Long projectId) {
		transactionService.runAfterCommit(() -> {
			submitToActiveServer(projectId, () -> {
				try {
					sessionService.run(() -> {
						var project = load(projectId);
						var repository = getRepository(projectId);

						for (var ref: GitUtils.getCommitRefs(repository, R_HEADS)) {
							var refName = ref.getName();
							if (RefUpdated.isValidRef(refName)) {
								var commitId = ref.getPeeledObj().copy();
								project.cacheObjectId(refName, commitId);
								listenerRegistry.post(new RefUpdated(project, refName, ObjectId.zeroId(), commitId));
							}
						}
					});
				} catch (Exception e) {
					logger.error("Error posting ref updated event", e);
				}					
				return null;				
			});
			
		});
	}
	
	private void checkGitDir(Long projectId) {
		File gitDir = getGitDir(projectId);
		if (gitDir.listFiles().length == 0) {
			logger.info("Initializing git repository in '" + gitDir + "'...");
			try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			storageService.initLfsDir(projectId);
		} else if (!isValid(gitDir)) {
			logger.warn("Directory '" + gitDir + "' is not a valid git repository, reinitializing...");
			cleanDir(gitDir);
			storageService.initLfsDir(projectId);
			try (Git git = Git.init().setDirectory(gitDir).setBare(true).call()) {
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			storageService.initLfsDir(projectId);
		}
	}

	private boolean updateStoredConfig(StoredConfig storedConfig, String section,
									   String name, @Nullable String value) {
		if (value != null) {
			if (!value.equals(storedConfig.getString(section, null, name))) {
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

	@Transactional
	@Listen
	public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
		cache = new ProjectCache(hazelcastInstance.getReplicatedMap("projectCache"));
		for (Project project : query()) {
			String path = project.calcPath();
			if (!path.equals(project.getPath()))
				project.setPath(path);
			cache.put(project.getId(), project.getFacade());
		}

		Map<Long, ProjectLastActivityDate> lastEventDates = new HashMap<>();
		for (ProjectLastActivityDate lastEventDate : lastEventDateService.query())
			lastEventDates.put(lastEventDate.getId(), lastEventDate);

		logger.info("Checking projects...");
		
		replicas = hazelcastInstance.getMap("projectReplicas");
		activeServers = hazelcastInstance.getMap("projectActiveServers");
		
		var projects = cache.clone();
		String localServer = clusterService.getLocalServerAddress();
		for (var projectDir: getProjectsDir().listFiles()) {
			if (new File(projectDir, DELETE_MARK).exists()) {
				logger.info("Deleting directory marked for deletion: " + projectDir);
				FileUtils.deleteDir(projectDir);
				continue;
			}
			if (!NumberUtils.isDigits(projectDir.getName())) 
				continue;
			var projectId = Long.valueOf(projectDir.getName());
			var project = projects.get(projectId);
			if (project != null) {
				logger.debug("Checking project (path: {})...", project.getPath());
				checkGitDir(projectId);
				HookUtils.checkHooks(getGitDir(projectId));
				checkGitConfig(projectId, project.getGitPackConfig());
				
				LinkedHashMap<String, ProjectReplica> newReplicasOfProject;
				var replica = new ProjectReplica();
				replica.loadType(projectDir);
				replica.setVersion(readVersion(projectDir));
				
				while (true) {
					var replicasOfProject = replicas.get(projectId);
					if (replicasOfProject != null) {
						newReplicasOfProject = new LinkedHashMap<>(replicasOfProject);
						newReplicasOfProject.put(localServer, replica);
						if (replicas.replace(projectId, replicasOfProject, newReplicasOfProject)) 
							break;
					} else {
						newReplicasOfProject = new LinkedHashMap<>();
						newReplicasOfProject.put(localServer, replica);
						if (replicas.putIfAbsent(projectId, newReplicasOfProject) == null) 
							break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				updateActiveServer(projectId, newReplicasOfProject, false);				
			}
		}
	}

	@Listen
	public void on(ConnectionEvent event) {
		if (clusterService.isLeaderServer()) {
			logger.info("Updating active servers upon cluster member change...");
			updateActiveServers();
		}
	}
	
	@Override
	public boolean isSharedDir(File dir, String remoteServer, Long projectId, String subPath) {
		var testFile = new File(dir, SHARE_TEST_DIR + "/" + UUID.randomUUID());
		FileUtils.touchFile(testFile);
		try {
			return clusterService.runOnServer(remoteServer, () -> {
				var remoteDir = getSubDir(projectId, subPath, false);
				return new File(remoteDir, SHARE_TEST_DIR + "/" + testFile.getName()).exists();
			});
		} finally {
			FileUtils.deleteFile(testFile);
		}
	}

	@Override
	public void updateActiveServers() {
		var newActiveServers = new HashMap<Long, String>();
		for (var entry: replicas) {
			var projectId = entry.getKey();
			newActiveServers.put(projectId, updateActiveServer(projectId, entry.getValue(), true));
		}
		notifyActiveServerChanged(newActiveServers);
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		var localServer = clusterService.getLocalServerAddress();
		for (var entry: replicas.entrySet()) {
			var projectId = entry.getKey();
			var replicasOfProject = entry.getValue();
			var replica = replicasOfProject.get(localServer);
			if (replica != null) {
				var activeServer = getActiveServer(projectId, false);
				if (activeServer != null) {
					if (activeServer.equals(localServer)) 
						requestToSyncReplicas(projectId, activeServer, replicasOfProject);
					else if (replica.getType() != REDUNDANT) 
						requestToSyncReplica(projectId, activeServer);
				}
			}
		}
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}
	
	@Listen
	public void on(SystemStopped event) {
		var localServer = clusterService.getLocalServerAddress();
		if (replicas != null) {
			var newActiveServers = new HashMap<Long, String>();
			for (var projectToReplicas : replicas.entrySet()) {
				var projectId = projectToReplicas.getKey();
				var replicasOfProject = projectToReplicas.getValue();
				while (true) {
					var newReplicasOfProject = new LinkedHashMap<>(replicasOfProject);
					if (newReplicasOfProject.remove(localServer) == null) {
						break;
					} else if (replicas.replace(projectId, replicasOfProject, newReplicasOfProject)) {
						newActiveServers.put(projectId, updateActiveServer(projectId, newReplicasOfProject, true));
						break;
					} else {
						replicasOfProject = replicas.get(projectId);
						if (replicasOfProject == null)
							break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
			notifyActiveServerChanged(newActiveServers);
		}
		
		synchronized (repositoryCache) {
			for (Repository repository : repositoryCache.values()) {
				repository.close();
			}
			repositoryCache.clear();
		}
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

	private Order getOrder(EntitySort sort, CriteriaBuilder builder, From<Project, Project> root) {
		if (sort.getDirection() == ASCENDING)
			return builder.asc(ProjectQuery.getPath(root, SORT_FIELDS.get(sort.getField()).getProperty()));
		else
			return builder.desc(ProjectQuery.getPath(root, SORT_FIELDS.get(sort.getField()).getProperty()));
	}

	@SuppressWarnings("rawtypes")
	private CriteriaQuery<Project> buildCriteriaQuery(Subject subject, Session session, EntityQuery<Project> projectQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Project> query = builder.createQuery(Project.class);
		Root<Project> root = query.from(Project.class);
		query.select(root);

		query.where(getPredicates(subject, projectQuery.getCriteria(), query, root, builder));

		List<Order> orders = new ArrayList<>();

		for (EntitySort sort : projectQuery.getSorts()) 
			orders.add(getOrder(sort, builder, root));

		if (projectQuery.getCriteria() != null)
			orders.addAll(projectQuery.getCriteria().getPreferOrders(builder, root));

		for (EntitySort sort : projectQuery.getBaseSorts()) 
			orders.add(getOrder(sort, builder, root));

		var found = false;
		for (var order: orders) {
			if (order.getExpression() instanceof SingularAttributePath) {
				var expr = (SingularAttributePath) order.getExpression();
				if (expr.getAttribute().getName().equals(ProjectLastActivityDate.PROP_VALUE) 
						&& expr.getPathSource() instanceof SingularAttributePath 
						&& ((SingularAttributePath) expr.getPathSource()).getAttribute().getName().equals(Project.PROP_LAST_ACTIVITY_DATE)) {
					found = true;
					break;
				}
			}
		}
		if (!found)
			orders.add(builder.desc(ProjectQuery.getPath(root, Project.PROP_LAST_ACTIVITY_DATE + "." + ProjectLastActivityDate.PROP_VALUE)));	
		
		query.orderBy(orders);

		return query;
	}

	private Predicate[] getPredicates(Subject subject, @Nullable Criteria<Project> criteria, CriteriaQuery<?> query,
									  From<Project, Project> from, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (!SecurityUtils.isAdministrator(subject)) {
			Collection<Project> projects = SecurityUtils.getAuthorizedProjects(subject, new AccessProject());
			if (!projects.isEmpty()) {
				predicates.add(forManyValues(builder, from.get(Project.PROP_ID),
						projects.stream().map(it -> it.getId()).collect(toSet()), getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}
		if (criteria != null)
			predicates.add(criteria.getPredicate(null, query, from, builder));
		return predicates.toArray(new Predicate[0]);
	}

	@Sessional
	@Override
	public List<Project> query(Subject subject, EntityQuery<Project> query, boolean loadLabels, int firstResult, int maxResults) {
		CriteriaQuery<Project> criteriaQuery = buildCriteriaQuery(subject, getSession(), query);
		Query<Project> projectQuery = getSession().createQuery(criteriaQuery);
		projectQuery.setFirstResult(firstResult);
		projectQuery.setMaxResults(maxResults);
		var projects = projectQuery.getResultList();

		if (!projects.isEmpty() && loadLabels) 
			labelService.populateLabels(projects);
		
		return projects;
	}

	@Sessional
	@Override
	public int count(Subject subject, Criteria<Project> projectCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Project> root = criteriaQuery.from(Project.class);

		criteriaQuery.where(getPredicates(subject, projectCriteria, criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public Collection<Long> getSubtreeIds(Long projectId) {
		return cache.getSubtreeIds(projectId);
	}

	@Override
	public Collection<Long> getPathMatchingIds(PatternSet patternSet) {
		return cache.getMatchingIds(patternSet);
	}

	@Override
	public Collection<Long> getIds() {
		return new HashSet<>(cache.keySet());
	}

	@Override
	public Predicate getPathMatchPredicate(CriteriaBuilder builder, Path<Project> path, String pathPattern) {
		return forManyValues(builder, path.get(Project.PROP_ID),
				cache.getMatchingIds(pathPattern), cache.keySet());
	}

	@Transactional
	@Override
	public void move(Collection<Project> projects, Project parent) {
		for (Project project : projects) {
			project.setParent(parent);
			update(project);
		}
	}
	
	@Override
	public List<ProjectFacade> getChildren(Long projectId) {
		return cache.getChildren(projectId);
	}

	@Override
	public boolean hasChildren(Long projectId) {
		return cache.hasChildren(projectId);
	}

	@Override
	public ProjectCache cloneCache() {
		return cache.clone();
	}

	@Override
	public ProjectFacade findFacade(Long projectId) {
		return cache.get(projectId);
	}

	@Override
	public String getFavoriteQuery(User user) {
		if (user != null && !user.getProjectQueryPersonalization().getQueries().isEmpty()) {
			return user.getProjectQueryPersonalization().getQueries().iterator().next().getQuery();
		} else {
			GlobalProjectSetting projectSetting = settingService.getProjectSetting();
			if (!projectSetting.getNamedQueries().isEmpty())
				return projectSetting.getNamedQueries().iterator().next().getQuery();
		}
		return null;
	}

	@Override
	public String getActiveServer(Long projectId, boolean mustExist) {
		var activeServer = activeServers.get(projectId);
		if (activeServer != null)
			return activeServer;
		else if (mustExist)
			throw new ExplicitException("Active server not found for project id: " + projectId);
		else
			return null;
	}
	
	@Override
	public Set<Long> getActiveIds() {
		var localServer = clusterService.getLocalServerAddress();
		return new HashSet<>(activeServers.project(Map.Entry::getKey, entry -> entry.getValue().equals(localServer)));
	}

	@Override
	public Map<String, Collection<Long>> groupByActiveServers(Collection<Long> projectIds) {
		Map<String, Collection<Long>> projectIdsByServer = new HashMap<>();
		for (var projectId: projectIds) {
			var activeServer = activeServers.get(projectId);
			if (activeServer != null) {
				var projectIdsOnServer = projectIdsByServer.computeIfAbsent(activeServer, k -> new HashSet<>());
				projectIdsOnServer.add(projectId);
			}
		}
		return projectIdsByServer;
	}

	private String updateActiveServer(Long projectId,
									  Map<String, ProjectReplica> replicasOfProject,
									  boolean syncReplicas) {
		var effectiveReplicasOfProject = new LinkedHashMap<String, ProjectReplica>();
		for (var entry : replicasOfProject.entrySet()) {
			if (clusterService.getServer(entry.getKey(), false) != null)
				effectiveReplicasOfProject.put(entry.getKey(), entry.getValue());
		}
		
		String oldActiveServer, newActiveServer = null;
		if (!effectiveReplicasOfProject.isEmpty()) {
			long maxVersion = effectiveReplicasOfProject.values().stream()
					.mapToLong(ProjectReplica::getVersion).max().getAsLong();
			var candidates = effectiveReplicasOfProject.entrySet().stream()
					.filter(it -> it.getValue().getVersion() == maxVersion)
					.collect(toList());
			for (var candidate : candidates) {
				if (candidate.getValue().getType() == PRIMARY) {
					newActiveServer = candidate.getKey();
					break;
				}
			}
			if (newActiveServer == null) {
				for (var candidate : candidates) {
					if (candidate.getValue().getType() == BACKUP) {
						newActiveServer = candidate.getKey();
						break;
					}
				}
			}
			if (newActiveServer == null)
				newActiveServer = candidates.iterator().next().getKey();
		}
		
		if (newActiveServer != null)
			oldActiveServer = activeServers.put(projectId, newActiveServer);
		else
			oldActiveServer = activeServers.remove(projectId);
		
		if (newActiveServer != null) {
			if (syncReplicas)
				requestToSyncReplicas(projectId, newActiveServer, replicasOfProject);
			if (!newActiveServer.equals(oldActiveServer))
				return newActiveServer;
		}
		return null;
	}
	
	private void notifyActiveServerChanged(Map<Long, String> newActiveServers) {
		var projectIds = new HashMap<String, Collection<Long>>();
		newActiveServers.forEach((projectId, server) -> {
			if (server != null) {
				var projectIdsOfServer = projectIds.computeIfAbsent(server, k -> new HashSet<>());
				projectIdsOfServer.add(projectId);
			}
		});
		projectIds.forEach((key, value) -> listenerRegistry.post(new ActiveServerChanged(key, value)));
	}
	
	@Override
	public <T> T runOnActiveServer(Long projectId, ClusterTask<T> task) {
		return clusterService.runOnServer(getActiveServer(projectId, true), task);
	}

	@Override
	public <T> Map<String, T> runOnReplicaServers(Long projectId, ClusterTask<T> task) {
		var replicasOfProject = replicas.get(projectId);
		if (replicasOfProject != null)
			return clusterService.runOnServers(replicasOfProject.keySet(), task);
		else 
			return new HashMap<>();
	}
	
	@Override
	public <T> Future<T> submitToActiveServer(Long projectId, ClusterTask<T> task) {
		return clusterService.submitToServer(getActiveServer(projectId, true), task);
	}

	@Override
	public <T> Map<String, Future<T>> submitToReplicaServers(Long projectId, ClusterTask<T> task) {
		var replicasOfProject = replicas.get(projectId);
		if (replicasOfProject != null) 
			return clusterService.submitToServers(replicasOfProject.keySet(), task);
		else 
			return new HashMap<>();
	}
	
	@Override
	public ProjectFacade findFacadeByPath(String path) {
		return cache.findByPath(path);
	}

	@Override
	public ProjectFacade findFacadeByKey(String key) {
		return cache.findByKey(key);
	}
	
	@Override
	public ProjectFacade findFacadeById(Long id) {
		return cache.get(id);
	}

	@Override
	public File getLfsObjectsDir(Long projectId) {
		return new File(getGitDir(projectId), "lfs/objects");
	}

	@Override
	public Collection<String> getReservedNames() {
		return reservedNames;
	}

	@Nullable
	@Override
	public ArtifactInfo getSiteArtifactInfo(Long projectId, String siteArtifactPath) {
		return runOnActiveServer(projectId, new ClusterTask<>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ArtifactInfo call() {
				return read(Project.getSiteLockName(projectId), () -> {
					File siteArtifact = new File(getSiteDir(projectId), siteArtifactPath);
					if (siteArtifact.exists()) {
						if (siteArtifact.isFile()) {
							String mediaType = Files.probeContentType(siteArtifact.toPath());
							if (mediaType == null)
								mediaType = MediaType.APPLICATION_OCTET_STREAM;
							return new FileInfo(siteArtifactPath, siteArtifact.lastModified(), siteArtifact.length(), mediaType);
						} else {
							return new DirectoryInfo(siteArtifactPath, siteArtifact.lastModified(), null);
						}
					} else {
						return null;
					}
				});
			}

		});
	}

	@Override
	public void redistributeReplicas() {
		var snapshot = new HashMap<>(replicas);
		clusterService.redistributeProjects(snapshot);	
		var newActiveServers = new HashMap<Long, String>();
		for (var newProjectToReplicas: snapshot.entrySet()) {
			var projectId = newProjectToReplicas.getKey();
			var project = cache.get(projectId);
			while (true) {
				var replicasOfProject = replicas.get(projectId);
				if (project != null && replicasOfProject != null) {
					var newReplicasOfProject = newProjectToReplicas.getValue();
					for (var newServerToReplica: newReplicasOfProject.entrySet()) {
						var server = newServerToReplica.getKey();
						var newReplica = newServerToReplica.getValue();
						var replica = replicasOfProject.get(server);
						if (replica == null || replica.getType() != newReplica.getType()) {
							newReplica.setVersion(clusterService.runOnServer(server, () -> {
								var projectDir = getProjectDir(projectId);
								if (!projectDir.exists()) {
									FileUtils.createDir(projectDir);
									initGit(projectId, project.getGitPackConfig());
								} else {
									FileUtils.deleteFile(new File(projectDir, DELETE_MARK));
								}
								newReplica.saveType(projectDir);
								return readVersion(projectDir);
							}));
						}
					}
					
					if (replicasOfProject.equals(newReplicasOfProject)) {
						break;
					} else if (replicas.replace(projectId, replicasOfProject, newReplicasOfProject)) {
						newActiveServers.put(projectId, updateActiveServer(projectId, newReplicasOfProject, true));
						break;
					}
				} else {
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		notifyActiveServerChanged(newActiveServers);
	}
	
	private void initGit(Long projectId, GitPackConfig gitPackConfig) {
		checkGitDir(projectId);
		var gitDir = getGitDir(projectId);
		HookUtils.checkHooks(gitDir);
		checkGitConfig(projectId, gitPackConfig);
	}
	
	@Listen
	public void on(RefUpdated event) {
		Long projectId = event.getProject().getId();
		directoryModified(projectId, getGitDir(projectId));
		if (!event.getNewCommitId().equals(ObjectId.zeroId()))
			writeLfsSinceCommits(projectId, Sets.newHashSet(event.getNewCommitId()));
	}

	@Listen
	public void on(DefaultBranchChanged event) {
		Long projectId = event.getProject().getId();
		directoryModified(projectId, getGitDir(projectId));
	}
	
	@Override
	public void directoryModified(Long projectId, File directory) {
		var projectDir = getProjectDir(projectId);
		var projectPath = projectDir.toPath();	
		var currentPath = directory.toPath();
		while (currentPath.startsWith(projectPath)) {
			var currentDir = currentPath.toFile();
			increaseVersion(currentDir);
			currentPath = currentPath.getParent();
		}
		updateReplicaVersion(projectId);
		
		var replicasOfProject = replicas.get(projectId);
		if (replicasOfProject != null)
			requestToSyncReplicas(projectId, clusterService.getLocalServerAddress(), replicasOfProject);
	}

	@Override
	public boolean hasLfsObjects(Long projectId) {
		var lfsDir = new File(getGitDir(projectId), "lfs/objects");
		return lfsDir.exists() && lfsDir.list().length != 0;
	}

	@Override
	public Map<String, ProjectReplica> getReplicas(Long projectId) {
		return replicas.get(projectId);
	}

	@Override
	public Collection<Long> getIdsWithoutEnoughReplicas() {
		var ids = new HashSet<Long>();
		for (var entry: replicas.entrySet()) {
			if (isWithoutEnoughReplicas(entry.getValue()))
				ids.add(entry.getKey());
		}
		return ids;
	}

	@Override
	public Collection<Long> getIdsHasOutdatedReplicas() {
		var ids = new HashSet<Long>();
		var activeServers = new HashMap<>(this.activeServers);
		for (var entry: replicas.entrySet()) {
			var activeServer = activeServers.get(entry.getKey());
			if (hasOutdatedReplicas(entry.getValue(), activeServer))
				ids.add(entry.getKey());
		}
		return ids;
	}

	@Override
	public boolean hasOutdatedReplicas(Long projectId) {
		var replicasOfProject = replicas.get(projectId);
		var activeServer = getActiveServer(projectId, false);
		if (replicasOfProject != null && activeServer != null) 
			return hasOutdatedReplicas(replicasOfProject, activeServer);
		else 
			return false;
	}

	private boolean hasOutdatedReplicas(Map<String, ProjectReplica> replicasOfProject, String activeServer) {
		var activeReplica = replicasOfProject.get(activeServer);
		return replicasOfProject.entrySet().stream().anyMatch(it -> clusterService.getServer(it.getKey(), false) != null && it.getValue().getType() != REDUNDANT && it.getValue().getVersion() < activeReplica.getVersion());
	}
	
	@Override
	public boolean isWithoutEnoughReplicas(Long projectId) {
		var replicasOfProject = replicas.get(projectId);
		if (replicasOfProject != null)
			return isWithoutEnoughReplicas(replicasOfProject);
		else 
			return false;
	}

	private boolean isWithoutEnoughReplicas(Map<String, ProjectReplica> replicasOfProject) {
		var count = replicasOfProject.entrySet().stream()
				.filter(it -> it.getValue().getType() != REDUNDANT && clusterService.getServer(it.getKey(), false) != null)
				.count();
		return count < settingService.getClusterSetting().getReplicaCount();
	}

	@Override
	public Collection<Long> getIdsMissingStorage() {
		var ids = getIds();
		ids.removeAll(activeServers.keySet());
		return ids;
	}

	@Override
	public boolean isMissingStorage(Long projectId) {
		return replicas.get(projectId) == null;
	}

	private void updateReplicaVersion(Long projectId) {
		var projectDir = getProjectDir(projectId);
		while (true) {
			var replicasOfProject = replicas.get(projectId);
			if (replicasOfProject != null) {
				var localServer = clusterService.getLocalServerAddress();
				var replica = replicasOfProject.get(localServer);
				if (replica != null) {
					var newReplicasOfProject = new LinkedHashMap<>(replicasOfProject);
					var newReplica = new ProjectReplica();
					newReplica.setType(replica.getType());
					newReplica.setVersion(readVersion(projectDir));
					newReplicasOfProject.put(localServer, newReplica);
					if (replicas.replace(projectId, replicasOfProject, newReplicasOfProject))
						break;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				break;
			}
		}
	}
	
	@Override
	public void requestToSyncReplica(Long projectId, String syncWithServer) {
		batchWorkExecutionService.submit(getSyncWorker(projectId), new SyncWork(SYNC_PRIORITY, syncWithServer));
	}

	private void requestToSyncReplicas(Long projectId, String syncWithServer, 
									   Map<String, ProjectReplica> replicasOfProject) {
		for (var serverToReplica: replicasOfProject.entrySet()) {
			var server = serverToReplica.getKey();
			var replica = serverToReplica.getValue();
			if (clusterService.getServer(server, false) != null 
					&& !server.equals(syncWithServer)
					&& replica.getType() != REDUNDANT) {
				clusterService.submitToServer(server, () -> {
					try {
						requestToSyncReplica(projectId, syncWithServer);
					} catch (Exception e) {
						logger.error("Error requesting replica sync of project with id '" + projectId + "'", e);
					}
					return null;
				});
			}
		}
	}
	
	private Lock getLfsSinceCommitsLock(Long projectId) {
		return LockUtils.getLock("lfs-since-commits:" + projectId);
	}
	
	@Override
	public Collection<ObjectId> readLfsSinceCommits(Long projectId) {
		Lock lock = getLfsSinceCommitsLock(projectId);
		lock.lock();
		try {
			Collection<ObjectId> commitIds = new HashSet<>();
			var file = new File(getGitDir(projectId), LFS_SINCE_COMMITS);
			if (file.exists()) {
				for (var line : FileUtils.readLines(file, UTF_8))
					commitIds.add(ObjectId.fromString(line));
			}
			return commitIds;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void writeLfsSinceCommits(Long projectId, Collection<ObjectId> commitIds) {
		Lock lock = getLfsSinceCommitsLock(projectId);
		lock.lock();
		try {
			var lines = commitIds.stream().map(ObjectId::getName).collect(toSet());
			var file = new File(getGitDir(projectId), LFS_SINCE_COMMITS);
			FileUtils.writeLines(file, UTF_8.name(), lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void syncDirectory(Long projectId, String path, Consumer<String> childSyncer, String activeServer) {
		var directory = new File(getProjectDir(projectId), path);
		
		long remoteVersion = clusterService.runOnServer(activeServer, () -> readVersion(new File(getProjectDir(projectId), path)));
		long version = readVersion(directory);
		
		if (version < remoteVersion) {
			Collection<String> remoteChildren = clusterService.runOnServer(activeServer, () -> {
				var children = new HashSet<String>();
				for (var file: new File(getProjectDir(projectId), path).listFiles()) {
					if (!isVersionFile(file))
						children.add(file.getName());
				}
				return children;
			});								
			
			FileUtils.createDir(directory);
			for (var file: directory.listFiles()) {
				if (!isVersionFile(file)) {
					if (remoteChildren.remove(file.getName()))
						childSyncer.accept(file.getName());
					else if (file.isFile())
						FileUtils.deleteFile(file);
					else
						FileUtils.deleteDir(file);
				}
			}
			for (var child: remoteChildren)
				childSyncer.accept(child);
			
			writeVersion(directory, remoteVersion);
		}
	}

	@Override
	public void syncDirectory(Long projectId, String path, String readLock, String activeServer) {
		var directory = new File(getProjectDir(projectId), path);
		long version = readVersion(directory);

		long remoteVersion = clusterService.runOnServer(activeServer, () -> {
			return readVersion(new File(getProjectDir(projectId), path));
		});

		if (version < remoteVersion) {
			FileUtils.cleanDir(directory);
			Client client = ClientBuilder.newClient();
			try {
				String fromServerUrl = clusterService.getServerUrl(activeServer);
				WebTarget target = client.target(fromServerUrl).path("/~api/cluster/project-files")
						.queryParam("projectId", projectId)
						.queryParam("path", path)
						.queryParam("patterns", "** -" + FILE_VERSION)
						.queryParam("readLock", readLock);
				Invocation.Builder builder = target.request();
				builder.header(AUTHORIZATION,
						BEARER + " " + clusterService.getCredential());

				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (InputStream is = response.readEntity(InputStream.class)) {
						TarUtils.untar(is, directory, false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				client.close();
			}
			writeVersion(directory, remoteVersion);
		}
	}

	@Override
	public void syncFile(Long projectId, String path, String readLock, String activeServer) {
		var file = new File(getProjectDir(projectId), path);
		Client client = ClientBuilder.newClient();
		try {
			String fromServerUrl = clusterService.getServerUrl(activeServer);
			WebTarget target = client.target(fromServerUrl).path("/~api/cluster/project-file")
					.queryParam("projectId", projectId)
					.queryParam("path", path)
					.queryParam("readLock", readLock);
			Invocation.Builder builder = target.request();
			builder.header(AUTHORIZATION,
					BEARER + " " + clusterService.getCredential());
			try (Response response = builder.get()) {
				if (response.getStatus() == NO_CONTENT.getStatusCode()) {
					if (file.exists())
						FileUtils.deleteFile(file);
				} else {
					FileUtils.createDir(file.getParentFile());
					KubernetesHelper.checkStatus(response);
					try (
							var is = response.readEntity(InputStream.class);
							var os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
						IOUtils.copy(is, os, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} finally {
			client.close();
		}
	}
	
	private BatchWorker getSyncWorker(Long projectId) {
		return new BatchWorker("project-" + projectId + "-sync") {

			private static final long serialVersionUID = 1L;

			@Override
			public void doWorks(List<Prioritized> works) {
				var syncWithServer = ((SyncWork) works.get(works.size() - 1)).syncWithServer;
				var project = cache.get(projectId);
				if (project != null) {
					try {
						var projectDir = getProjectDir(projectId);
						var remoteVersion = clusterService.runOnServer(syncWithServer, () -> readVersion(getProjectDir(projectId)));
						var version = readVersion(projectDir);

						if (version < remoteVersion) {
							logger.debug("Syncing project (project: {}, server: {})...", project.getPath(), syncWithServer);
							syncGit(projectId, syncWithServer);
							attachmentService.syncAttachments(projectId, syncWithServer);
							buildService.syncBuilds(projectId, syncWithServer);
							visitInfoService.syncVisitInfo(projectId, syncWithServer);
							packBlobService.syncPacks(projectId, syncWithServer);
							
							syncDirectory(projectId, SITE_DIR, getSiteLockName(projectId), syncWithServer);
							
							writeVersion(projectDir, remoteVersion);
							logger.debug("Project synced (project: {}, server: {})", project.getPath(), syncWithServer);
						}
						updateReplicaVersion(projectId);
					} catch (Exception e) {
						logger.error(String.format("Error syncing (project: %s, server: %s)", project.getPath(), syncWithServer), e);
					}
				}
			}

			private void syncGit(Long projectId, String activeServer) {
				var gitDir = getGitDir(projectId);
				var remoteGitVersion = clusterService.runOnServer(activeServer, () -> readVersion(getGitDir(projectId)));
				var gitVersion = readVersion(gitDir);
				if (gitVersion < remoteGitVersion) {
					var repository = getRepository(projectId);
					var defaultBranch = getDefaultBranch(repository);
					var remoteDefaultBranch = clusterService.runOnServer(activeServer, () -> getDefaultBranch(getRepository(projectId)));
					var withLfs = clusterService.runOnServer(activeServer, () -> hasLfsObjects(projectId));

					if (remoteDefaultBranch != null) {
						CommandUtils.callWithClusterCredential(new GitTask<>() {

							private void fetch(Commandline git, String fetchUrl) {
								git.addArgs("fetch", "--force", fetchUrl, "refs/*:refs/*", "-pP");
								git.execute(new LineConsumer() {
									@Override
									public void consume(String line) {
										logger.debug(line);
									}
								}, new LineConsumer() {
									@Override
									public void consume(String line) {
										if (!line.startsWith("From") && !line.contains("->"))
											logger.error(line);
										else
											logger.debug(line);
									}
								}).checkReturnCode();
							}

							@Override
							public Object call(Commandline git) throws IOException {
								git.workingDir(repository.getDirectory());
								var fetchUrl = clusterService.getServerUrl(activeServer) + "/" + cache.get(projectId).getPath();
								fetch(git, fetchUrl);
								git.clearArgs();

								if (withLfs) {
									var lfsDir = storageService.initLfsDir(projectId);
									if (isSharedDir(lfsDir, activeServer, projectId, "git/lfs")) {
										fetch(git, fetchUrl);
									} else {
										var sinceCommitIds = readLfsSinceCommits(projectId);
										var untilCommitIds = new HashSet<ObjectId>();
										for (Ref ref: repository.getRefDatabase().getRefs())
											untilCommitIds.add(ref.getObjectId());

										if (sinceCommitIds.isEmpty()) {
											new LfsFetchAllCommand(git.workingDir(), fetchUrl) {
												@Override
												protected Commandline newGit() {
													return git;
												}
											}.run();
										} else {
											var fetchCommitIds = getReachableCommits(repository, sinceCommitIds, untilCommitIds)
													.stream().map(AnyObjectId::copy).collect(toList());
											new LfsFetchCommand(git.workingDir(), fetchUrl, fetchCommitIds) {
												@Override
												protected Commandline newGit() {
													return git;
												}
											}.run();
										}
										writeLfsSinceCommits(projectId, untilCommitIds);
									}
								} else {
									fetch(git, fetchUrl);
								}
								return null;
							}
						});

						if (!remoteDefaultBranch.equals(defaultBranch))
							setDefaultBranch(repository, remoteDefaultBranch);
					}
					writeVersion(gitDir, remoteGitVersion);
				}
			}
			
		};
	}

	@Override
	public File getProjectsDir() {
		File projectsDir = new File(Bootstrap.getSiteDir(), "projects");
		FileUtils.createDir(projectsDir);
		return projectsDir;
	}

	@Override
	public File getProjectDir(Long projectId) {
		return new File(getProjectsDir(), String.valueOf(projectId));
	}

	@Override
	public File getSubDir(Long projectId, String subdirPath) {
		return getSubDir(projectId, subdirPath, true);
	}

	@Override
	public File getSubDir(Long projectId, String subdirPath, boolean createIfNotExist) {
		File projectDir = getProjectDir(projectId);
		if (projectDir.exists()) {
			File subDir = new File(projectDir, subdirPath);
			if (createIfNotExist)
				FileUtils.createDir(subDir);
			return subDir;
		} else {
			throw new ExplicitException("Storage directory not found for project id " + projectId);
		}
	}
	
	@Override
	public File getGitDir(Long projectId) {
		return getSubDir(projectId, "git");
	}

	@Override
	public File getInfoDir(Long projectId) {
		return getSubDir(projectId, "info");
	}

	@Override
	public File getIndexDir(Long projectId) {
		return getSubDir(projectId, "index");
	}

	@Override
	public File getCacheDir(Long projectId) {
		return getSubDir(projectId, "cache");
	}

	@Override
	public File getSiteDir(Long projectId) {
		return getSubDir(projectId, SITE_DIR);
	}

	@Override
	public File getAttachmentDir(Long projectId) {
		return getSubDir(projectId, ATTACHMENT_DIR);
	}
	
	@Override
	public void execute() {
		batchWorkExecutionService.submit(new BatchWorker("project-manager-house-keeping") {

			@Override
			public void doWorks(List<Prioritized> works) {
				if (clusterService.isLeaderServer()) {
					var newActiveServers = new HashMap<Long, String>();
					var replicaCount = settingService.getClusterSetting().getReplicaCount();
					for (var projectToReplicas: replicas.entrySet()) {
						var projectId = projectToReplicas.getKey();
						var replicasOfProject = projectToReplicas.getValue();
						while (true) {
							var newReplicasOfProject = new LinkedHashMap<>(replicasOfProject);
							if (!newReplicasOfProject.isEmpty()) {
								var activeServer = getActiveServer(projectId, false);
								var replica = newReplicasOfProject.get(activeServer);
								if (replica == null || replica.getType() != PRIMARY)
									newActiveServers.put(projectId, updateActiveServer(projectId, newReplicasOfProject, true));
								var maxVersion = newReplicasOfProject.values().stream()
										.map(ProjectReplica::getVersion)
										.max(naturalOrder())
										.get();
								var upToDateReplicaCount = newReplicasOfProject.values().stream()
										.filter(it -> it.getVersion() == maxVersion && it.getType() != REDUNDANT)
										.count();
								if (upToDateReplicaCount >= replicaCount) {
									var redundantServers = newReplicasOfProject.entrySet().stream()
											.filter(it -> it.getValue().getType() == REDUNDANT)
											.map(Map.Entry::getKey)
											.collect(toList());
									newReplicasOfProject.keySet().removeAll(redundantServers);
									if (!newReplicasOfProject.equals(replicasOfProject)) {
										if (replicas.replace(projectId, replicasOfProject, newReplicasOfProject)) {
											redundantServers.forEach(it -> clusterService.submitToServer(it, () -> {
												markStorageForDelete(projectId);
												return null;
											}));									
											break;
										} else {
											replicasOfProject = replicas.get(projectId);
											if (replicasOfProject == null)
												break;
										}
									} else {
										break;
									}
								} else {
									break;
								}
							} else {
								break;
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}
					notifyActiveServerChanged(newActiveServers);
				}
		
			}

		}, new Prioritized(HOUSE_KEEPING_PRIORITY));		
	}
	
	private void markStorageForDelete(Long projectId) {
		try {
			var projectDir = getProjectDir(projectId);
			if (projectDir.exists())
				new File(projectDir, DELETE_MARK).createNewFile();
		} catch (Exception e) {
			logger.error("Error marking storage directory of project with id '" + projectId + "' for deletion", e);
		}
	}
	
	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	private static class SyncWork extends Prioritized {

		final String syncWithServer;

		SyncWork(int priority, String syncWithServer) {
			super(priority);
			this.syncWithServer = syncWithServer;
		}

	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(ProjectService.class);
	}
	
}
