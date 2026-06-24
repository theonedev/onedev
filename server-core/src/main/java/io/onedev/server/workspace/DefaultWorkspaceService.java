package io.onedev.server.workspace;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.jspecify.annotations.Nullable;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hazelcast.map.IMap;

import io.onedev.agent.AgentUtils;
import io.onedev.agent.workspace.FileData;
import io.onedev.agent.workspace.GitExecutionResult;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.DefaultCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.cluster.NodeStopping;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.workspace.WorkspaceTerminalOpened;
import io.onedev.server.event.project.workspace.WorkspaceActive;
import io.onedev.server.event.project.workspace.WorkspaceCreated;
import io.onedev.server.event.project.workspace.WorkspaceEvent;
import io.onedev.server.event.project.workspace.WorkspaceInactive;
import io.onedev.server.event.project.workspace.WorkspacePending;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotFoundException;
import io.onedev.server.logging.LogService;
import io.onedev.server.logging.ServerLogger;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.model.support.administration.DockerAware;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.WorkspaceSpec;
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.CreateWorkspaces;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.impl.BaseEntityService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.ProjectWorkspaceStatusStat;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.interpolative.WorkspaceVariableInterpolator;
import io.onedev.server.web.component.terminal.ShellExit;
import io.onedev.server.web.editable.EditableUtils;

@Singleton
public class DefaultWorkspaceService extends BaseEntityService<Workspace> 
		implements WorkspaceService, Runnable, SchedulableTask, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultWorkspaceService.class);

	private static final int CHECK_INTERVAL = 5;

	private static final String WORKSPACE_LOGS_DIR = "workspace-logs";

	private static final String AUTO_DISCOVERED_PROVISIONER_NAME = "auto-discovered";

	private static final String RUN_PROMPT_SUCCESS_MARKER = "__ONEDEV_RUN_PROMPT_SUCCESS__";

	private static final String RUN_PROMPT_FAILURE_MARKER = "__ONEDEV_RUN_PROMPT_FAILURE__";

	@Inject
	private ProjectService projectService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Inject
	private LogService logService;

	@Inject
	private SessionService sessionService;

	@Inject
	private SettingService settingService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private UrlService urlService;

	@Inject
	private ExecutorService executorService;

	@Inject
	private TaskScheduler taskScheduler;

	@Inject
	private Set<WorkspaceProvisionerDiscoverer> workspaceProvisionerDiscoverers;

	private SequenceGenerator numberGenerator;

	private final Map<String, WorkspaceContext> workspaceContexts = new ConcurrentHashMap<>();

	private volatile IMap<Long, String> workspaceServers;

	private final Map<Long, Future<Void>> workspaceFutures = new ConcurrentHashMap<>();

	private final Map<Long, WorkspaceRuntime> workspaceRuntimes = new ConcurrentHashMap<>();

	private final Map<Long, Future<Void>> workspaceTaskFutures = new ConcurrentHashMap<>();

	private final Map<IWebSocketConnection, OutputState> workspaceConnections = new ConcurrentHashMap<>();
	
	// With tmux, we no longer need to replay the output. However we also send error messages 
	// (for instance when tmux is not installed) to terminal, so we still need to replay the 
	// output to ensure the error messages are displayed when websocket connection is 
	// established after shell reports error
	private final Map<String, ReplayBuffer> terminalReplayBuffers = new ConcurrentHashMap<>();

	private volatile Thread thread;

	private volatile boolean checkImmediately;

	private volatile String taskId;

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(WorkspaceService.class);
	}

	private synchronized SequenceGenerator getNumberGenerator() {
		if (numberGenerator == null)
			numberGenerator = new SequenceGenerator(Workspace.class, clusterService, dao);
		return numberGenerator;
	}

	@Sessional
	@Override
	public Workspace find(Project project, long number) {
		EntityCriteria<Workspace> criteria = newCriteria();
		criteria.add(Restrictions.eq("numberScope", project.getForkRoot()));
		criteria.add(Restrictions.eq(AbstractEntity.PROP_NUMBER, number));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public Workspace create(User user, Project project, ObjectId commitId, @Nullable String branch, String specName, boolean forTaskAutomation) {
		var workspace = new Workspace();
		workspace.setUser(user);
		workspace.setProject(project);
		workspace.setCommitHash(commitId.name());
		workspace.setBranch(branch);
		workspace.setSpecName(specName);
		workspace.setForTaskAutomation(forTaskAutomation);
		workspace.setToken(UUID.randomUUID().toString());
		workspace.setNumberScope(workspace.getProject().getForkRoot());
		workspace.setNumber(getNumberGenerator().getNextSequence(workspace.getNumberScope()));
		dao.persist(workspace);
		listenerRegistry.post(new WorkspaceCreated(workspace));
		return workspace;
	}

	@Transactional
	@Override
	public void update(Workspace workspace) {
		Preconditions.checkState(!workspace.isNew());
		dao.persist(workspace);
	}

	@Sessional
	@Listen
	public void on(WorkspaceEvent event) {
		if (event instanceof WorkspaceCreated || event instanceof WorkspacePending) {
			clusterService.submitToServer(clusterService.getLeaderServerAddress(), () -> {
				checkImmediately = true;
				return null;
			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		getNumberGenerator().removeNextSequence(project);
		} else if (event.getEntity() instanceof Workspace) {
			var workspace = (Workspace) event.getEntity();
			var loggingSupport = workspace.getLoggingSupport();
			var projectId = workspace.getProject().getId();
			var workspaceNumber = workspace.getNumber();
			var workspaceId = workspace.getId();
			var provisionerName = workspace.getProvisionerName();
			var pinnedServerAddress = workspace.getServerAddress();
			var pinnedAgentId = Agent.idOf(workspace.getAgent());
			var provisioner = resolveProvisionerForCleanup(workspace);

			// record project server since workspace deletion can be called while
			// deleting a project
			var projectServer = projectService.getActiveServer(projectId, true);
			
			if (projectServer != null) {
				transactionService.runAfterCommit(() -> {
					clusterService.submitToServer(projectServer, (ClusterTask<Void>) () -> {
						try {
							checkImmediately = true;

							// Delete workspace storage after workspace server is removed to
							// give a chance for cache to be uploaded
							while (workspaceServers.containsKey(workspaceId))
								Thread.sleep(1000);

							if (provisioner != null) {
								try {
									provisioner.deleteWorkspace(projectId, workspaceNumber, pinnedServerAddress, pinnedAgentId);
								} catch (Throwable t) {
									var message = "Error cleanup workspace via provisioner '%s' (project id: %d, workspace number: %d)"
											.formatted(provisionerName, projectId, workspaceNumber);
									logger.error(message, t);
								}	
							} else if (AUTO_DISCOVERED_PROVISIONER_NAME.equals(provisionerName)) {
								if (pinnedAgentId != null)
									AgentProvisionerUtils.deleteWorkspace(projectId, workspaceNumber, pinnedAgentId);
								else
									ServerProvisionerUtils.deleteWorkspace(projectId, workspaceNumber, pinnedServerAddress);
							} else if (provisionerName != null) {
								logger.warn("Provisioner '{}' not found, skipping deleting workspace storage (project id: {}, workspace number: {})",
										provisionerName, projectId, workspaceNumber);
							} else {
								logger.warn("Provisioner unknown. Skipping deleting workspace storage (project id: {}, workspace number: {})", projectId, workspaceNumber);
							}

							logService.flush(loggingSupport);

							var logFile = getLogFile(projectId, workspaceNumber);
							if (logFile.exists()) 
								FileUtils.deleteFile(logFile);							
						} catch (Throwable t) {
							var message = "Error deleting workspace storage (project id: %d, workspace number: %d)"
									.formatted(projectId, workspaceNumber);
							logger.error(message, t);
						}
						return null;
					});
				});
			}
		}
	}

	private Collection<Predicate> getPredicates(Subject subject, @Nullable Project project,
                                                From<Workspace, Workspace> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get(Workspace.PROP_PROJECT), project));
		} else if (!SecurityUtils.isAdministrator(subject)) {
			Collection<Project> projects = SecurityUtils.getAuthorizedProjects(subject, new CreateWorkspaces());
			if (!projects.isEmpty()) {
				Path<Long> projectIdPath = root.get(Workspace.PROP_PROJECT).get(Project.PROP_ID);
				predicates.add(Criteria.forManyValues(builder, projectIdPath,
						projects.stream().map(it -> it.getId()).collect(Collectors.toSet()),
						projectService.getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}
		return predicates;
	}

	private Predicate[] getPredicates(Subject subject, @Nullable Project project,
                                      @Nullable Criteria<Workspace> criteria, CriteriaQuery<?> query,
                                      From<Workspace, Workspace> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = getPredicates(subject, project, root, builder);
		if (criteria != null)
			predicates.add(criteria.getPredicate(null, query, root, builder));
		return predicates.toArray(new Predicate[0]);
	}

	@Sessional
	@Override
	public List<Workspace> query(Subject subject, @Nullable Project project, String numberQuery, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Workspace> criteriaQuery = builder.createQuery(Workspace.class);
		Root<Workspace> root = criteriaQuery.from(Workspace.class);
		criteriaQuery.select(root);

		Collection<Predicate> predicates = getPredicates(subject, project, root, builder);
		if (!numberQuery.isEmpty()) {
			try {
				predicates.add(builder.equal(root.get(Workspace.PROP_NUMBER), Long.parseLong(numberQuery)));
			} catch (NumberFormatException ignored) {
				predicates.add(builder.disjunction());
			}
		}
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.desc(root.get(Workspace.PROP_NUMBER)));

		org.hibernate.query.Query<Workspace> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.getResultList();
	}

	@Sessional
	@Override
	public List<Workspace> query(Subject subject, @Nullable Project project,
								 WorkspaceQuery query, int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Workspace> criteriaQuery = builder.createQuery(Workspace.class);
		Root<Workspace> root = criteriaQuery.from(Workspace.class);
		criteriaQuery.select(root);

		criteriaQuery.where(getPredicates(subject, project, query.getCriteria(),
				criteriaQuery, root, builder));

		applyOrders(root, criteriaQuery, builder, query);

		Query<Workspace> hibernateQuery = getSession().createQuery(criteriaQuery);
		hibernateQuery.setFirstResult(firstResult);
		hibernateQuery.setMaxResults(maxResults);
		return hibernateQuery.getResultList();
	}

	@Sessional
	@Override
	public int count(Subject subject, @Nullable Project project, Criteria<Workspace> criteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Workspace> root = criteriaQuery.from(Workspace.class);
		criteriaQuery.select(builder.count(root));

		criteriaQuery.where(getPredicates(subject, project, criteria, criteriaQuery, root, builder));

		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public int count(Project project, String branchName) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Workspace> root = criteriaQuery.from(Workspace.class);
		criteriaQuery.select(builder.count(root));
		criteriaQuery.where(builder.and(
				builder.equal(root.get(Workspace.PROP_PROJECT), project),
				builder.equal(root.get(Workspace.PROP_BRANCH), branchName)));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public List<ProjectWorkspaceStatusStat> queryStatusStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectWorkspaceStatusStat> criteriaQuery = builder.createQuery(ProjectWorkspaceStatusStat.class);
			Root<Workspace> root = criteriaQuery.from(Workspace.class);
			criteriaQuery.multiselect(
					root.get(Workspace.PROP_PROJECT).get(Project.PROP_ID),
					root.get(Workspace.PROP_STATUS), builder.count(root));
			criteriaQuery.groupBy(root.get(Workspace.PROP_PROJECT), root.get(Workspace.PROP_STATUS));
			criteriaQuery.where(root.get(Workspace.PROP_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(Workspace.PROP_STATUS)));
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Transactional
	@Override
	public void delete(Collection<Workspace> workspaces) {
		for (var workspace : workspaces) 
			delete(workspace);
	}

	@Transactional
	@Override
	public void reset(Workspace workspace) {
		Preconditions.checkState(workspace.getStatus() == Status.INACTIVE);
		workspace.setStatus(Status.PENDING);
		workspace.setInactiveDate(null);
		workspace.setActiveDate(null);
		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		
		projectService.runOnActiveServer(projectId, (ClusterTask<Void>) () -> {
			FileUtils.deleteFile(getLogFile(projectId, workspaceNumber));
			return null;
		});
		
		update(workspace);
		listenerRegistry.post(new WorkspacePending(workspace));
	}

	private void applyOrders(Root<Workspace> root, CriteriaQuery<?> criteriaQuery,
                             CriteriaBuilder builder, EntityQuery<Workspace> sessionQuery) {
		List<Order> orders = new ArrayList<>();
		for (EntitySort sort : sessionQuery.getSorts()) {
			var sortField = Workspace.SORT_FIELDS.get(sort.getField());
			if (sort.getDirection() == ASCENDING)
				orders.add(builder.asc(EntityQuery.getPath(root, sortField.getProperty())));
			else
				orders.add(builder.desc(EntityQuery.getPath(root, sortField.getProperty())));
		}
		if (!orders.isEmpty())
			criteriaQuery.orderBy(orders);
	}

	@Listen
	public void on(SystemStarting event) {
		var hazelcastInstance = clusterService.getHazelcastInstance();
		workspaceServers = hazelcastInstance.getMap("workspaceServers");
	}

	@Listen
	public void on(SystemStarted event) {				
		taskId = taskScheduler.schedule(this);
		thread = new Thread(this);
		thread.start();
	}

	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);		
		Thread copy = thread;
		thread = null;

		logger.info("Stopping workspaces...");
		for (var taskFuture: workspaceTaskFutures.values()) 
			taskFuture.cancel(true);

		/*
		 * Wait till all workspaces running on this server are stopped and finished uploading user data 
		 * and caches. For workspaces initiated from current server, they will be cancelled above; for
		 * workspaces initiated from other servers, they will be cancelled via NodeStopping event sent 
		 * to other servers
		 */
		while (!workspaceContexts.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
		}
		
		if (copy != null) {
			try {
				copy.join();
			} catch (InterruptedException ignored) {
			}
		}
	}

	@Listen
	public void on(NodeStopping event) {
		workspaceServers.entrySet().stream()
				.filter(it -> it.getValue().equals(event.getServer()))
				.forEach(it -> {
					var future = workspaceFutures.get(it.getKey());
					if (future != null)
						future.cancel(true);
				});
	}

	@SuppressWarnings("unchecked")
	@Sessional
	protected Map<Long, Long> queryPendingOrActive() {
		Query<?> query = getSession().createQuery("select id, project.id from Workspace where "
				+ "status=:pending or status=:active");
		query.setParameter("pending", Workspace.Status.PENDING);
		query.setParameter("active", Workspace.Status.ACTIVE);
		
		Map<Long, Long> result = new HashMap<>();
		for (Object[] fields: (List<Object[]>)query.list()) 
			result.put((Long)fields[0], (Long)fields[1]);
		return result;
	}

	@Override
	public void run() {
		var localServer = clusterService.getLocalServerAddress();
		
		while (!workspaceFutures.isEmpty() || thread != null) {
			try {
				if (clusterService.isLeaderServer()) {
					Map<String, Collection<Long>> workspaceIds = new HashMap<>();
					for (var entry : queryPendingOrActive().entrySet()) {
						var workspaceId = entry.getKey();
						var projectId = entry.getValue();
						var activeServer = projectService.getActiveServer(projectId, false);
						if (activeServer != null && clusterService.getOnlineServers().contains(activeServer)) {
							var workspaceIdsOfServer = workspaceIds.computeIfAbsent(activeServer, k -> new ArrayList<>());
							workspaceIdsOfServer.add(workspaceId);
						}
					}

					Collection<Future<?>> futures = new ArrayList<>();
					for (var entry : workspaceIds.entrySet()) {
						var server = entry.getKey();
						var workspaceIdsOfServer = entry.getValue();
						futures.add(clusterService.submitToServer(server, () -> {
							try {						
								while (thread == null) {
									if (OneDev.getInstance().isStopping())
										return null;
									Thread.sleep(1000);	
								}
								transactionService.run(() -> {
									for (var workspaceId : workspaceIdsOfServer) {
										var workspace = load(workspaceId);
										if (workspace.getStatus() == Status.PENDING) {
											var future = workspaceFutures.get(workspace.getId());
											if (future == null && thread != null) {
												try {
													workspaceFutures.put(workspace.getId(), run(workspace));
												} catch (Throwable t) {
													var workspaceLogger = logService.newLogger(workspace.getLoggingSupport());
													var explicitException = ExceptionUtils.find(t, ExplicitException.class);
													if (explicitException != null)
														workspaceLogger.error("Error provisioning workspace: " + explicitException.getMessage());
													else
														workspaceLogger.error("Error provisioning workspace", t);
													markWorkspaceInactive(workspace);
												}
											}
										} else if (workspace.getStatus() == Status.ACTIVE) {
											if (workspaceFutures.get(workspace.getId()) == null) {
												workspace.setActiveDate(null);
												workspace.setStatus(Status.PENDING);
												listenerRegistry.post(new WorkspacePending(workspace));
											}
										}
									}
								});
							} catch (Throwable t) {
								logger.error("Error checking pending/active workspaces", t);
							}
							return null;
						}));
					}
					for (var future : futures) 
						future.get();
				}
				
				sessionService.run(() -> {
					for (var it = workspaceFutures.entrySet().iterator(); it.hasNext(); ) {
						var entry = it.next();
						var workspace = get(entry.getKey());
						var future = entry.getValue();
						if (workspace == null 
								|| workspace.getStatus() == Status.INACTIVE 
								|| !localServer.equals(projectService.getActiveServer(workspace.getProject().getId(), false))) {
							future.cancel(true);
							it.remove();
						} else if (future.isDone()) {
							var workspaceLogger = logService.newLogger(workspace.getLoggingSupport());
							try {
								future.get();
								workspaceLogger.error("Workspace stopped for unknown reason");
							} catch (Throwable t) {
								if (ExceptionUtils.find(t, CancellationException.class) != null
										|| ExceptionUtils.find(t, InterruptedException.class) != null) {
									workspaceLogger.error("Workspace stopped as the server or agent running it shutted down");
								} else {
									var explicitException = ExceptionUtils.find(t, ExplicitException.class);
									if (explicitException != null)
										workspaceLogger.error("Workspace stopped: " + explicitException.getMessage());
									else
										workspaceLogger.error("Workspace stopped", t);
								}
							} finally {
								it.remove();
								markWorkspaceInactive(workspace);
							}
						}
					}
				});
				int count = 0;
				while (!checkImmediately && count++ < CHECK_INTERVAL) 
					Thread.sleep(1000);
				checkImmediately = false;
			} catch (Throwable t) {
				if (ExceptionUtils.find(t, ServerNotFoundException.class) == null 
						&& ExceptionUtils.find(t, InterruptedException.class) == null) {
					logger.error("Error checking workspaces", t);
				}
			}
		}
	}

	@Override
	public void execute() {
		for (var it = workspaceConnections.entrySet().iterator(); it.hasNext();) {
			if (!it.next().getKey().isOpen())
				it.remove();
		}
		for (var it = terminalReplayBuffers.entrySet().iterator(); it.hasNext();) {
			var terminalKey = it.next().getKey();
			var terminalKeyParts = terminalKey.split(":");
			var workspaceId = Long.parseLong(terminalKeyParts[0]);
			var shellId = terminalKeyParts[1];
			var runtime = workspaceRuntimes.get(workspaceId);
			if (runtime == null || !runtime.getShellLabels().containsKey(shellId)) 
				it.remove();
		}

		if (clusterService.isLeaderServer()) {
			var activeWorkspaceIds = getActiveWorkspaceIds();
			workspaceServers.removeAll(it -> !activeWorkspaceIds.contains(it.getKey()));
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatHourlyForever();
	}

	private boolean isApplicable(Workspace workspace, WorkspaceSpec spec, WorkspaceProvisioner provisioner) {
		if (spec.isRunInContainer() != (provisioner instanceof DockerAware))
			return false;
		else		
			return provisioner.isApplicable(workspace.getProject());
	}

	@Nullable
	private WorkspaceProvisioner resolveProvisionerForCleanup(Workspace workspace) {
		var provisionerName = workspace.getProvisionerName();
		if (provisionerName == null)
			return null;
		var provisioner = settingService.getWorkspaceProvisioners().stream()
				.filter(it -> provisionerName.equals(it.getName()))
				.findFirst()
				.orElse(null);
		if (provisioner != null)
			return provisioner;
		if (!AUTO_DISCOVERED_PROVISIONER_NAME.equals(provisionerName))
			return null;
		var spec = workspace.getSpec();
		if (spec == null)
			return null;
		spec = new WorkspaceVariableInterpolator(workspace).interpolateProperties(spec);
		return discoverProvisioner(workspace, spec);
	}

	@Nullable
	private WorkspaceProvisioner discoverProvisioner(Workspace workspace, WorkspaceSpec spec) {
		List<WorkspaceProvisionerDiscoverer> discoverers = new ArrayList<>(workspaceProvisionerDiscoverers);
		discoverers.sort(Comparator.comparing(WorkspaceProvisionerDiscoverer::getOrder));
		for (var discoverer : discoverers) {
			WorkspaceProvisioner provisioner = discoverer.discover();
			if (provisioner != null) {
				provisioner.setName(AUTO_DISCOVERED_PROVISIONER_NAME);
				if (isApplicable(workspace, spec, provisioner))
					return provisioner;
			}
		}
		return null;
	}

	private WorkspaceProvisioner getProvisioner(Workspace workspace, WorkspaceSpec spec, TaskLogger logger) {
		if (spec.getProvisioner() != null) {
			var provisioner = settingService.getWorkspaceProvisioners().stream()
				.filter(it -> it.getName().equals(spec.getProvisioner()))
				.findFirst()
				.orElseThrow(() -> new ExplicitException("Unable to find specified workspace provisioner '" + spec.getProvisioner() + "'"));
			if (!provisioner.isEnabled())
				throw new ExplicitException("Specified workspace provisioner '" + spec.getProvisioner() + "' is disabled");
			else if (!isApplicable(workspace, spec, provisioner))
				throw new ExplicitException("Specified workspace provisioner '" + spec.getProvisioner() + "' is not applicable for current workspace");
			else
				return provisioner;
		} else if (!settingService.getWorkspaceProvisioners().isEmpty()) {
			return settingService.getWorkspaceProvisioners().stream()
				.filter(it -> it.isEnabled() && isApplicable(workspace, spec, it))
				.findFirst()
				.orElseThrow(() -> new ExplicitException("No applicable workspace provisioner"));
		} else {
			logger.log("No workspace provisioner defined, auto-discovering...");
			var provisioner = discoverProvisioner(workspace, spec);
			if (provisioner != null) {
				logger.log("Discovered " + EditableUtils.getDisplayName(provisioner.getClass()).toLowerCase());
				return provisioner;
			}

			if (spec.isRunInContainer()) {
				throw new ExplicitException("""
					No applicable provisioner discovered for current workspace. \
					Please check if docker is installed on OneDev server, or configure \
					a kubernetes provisioner if you would like workspaces to run as pods""");
			} else {
				throw new ExplicitException("""
					No applicable provisioner discovered for current workspace. \
					Please add a shell provisioner in menu 'Administration / Workspace Provisioners'""");
			}
		}
	}

	private Future<Void> run(Workspace workspace) {
		Workspace.push(workspace);
		try {
			var spec = workspace.getSpec();
			if (spec == null)
				throw new ExplicitException("Spec not found in workspace project hierarchy");

			var interpolator = new WorkspaceVariableInterpolator(workspace);
			spec = interpolator.interpolateProperties(spec);

			String token = workspace.getToken();
			var project = workspace.getProject();
			var projectId = project.getId();
			var projectPath = project.getPath();
			var projectGitDir = projectService.getGitDir(project.getId()).getAbsolutePath();
			var workspaceId = workspace.getId();
			var workspaceNumber = workspace.getNumber();

			var workspaceLogger = logService.newLogger(workspace.getLoggingSupport());
			var provisioner = getProvisioner(workspace, spec, workspaceLogger);

			var gitEmail = workspace.getUser().getGitEmailAddress();
			if (gitEmail == null)
				throw new ExplicitException("No email address for git operations configured");
			
			var cloneInfo = new DefaultCloneInfo(urlService.cloneUrlFor(workspace.getProject(), false), token);		

			var context = new WorkspaceContext(spec, provisioner, token, projectId, projectPath, projectGitDir, 
					workspaceId, workspaceNumber, workspace.getUser().getId(), workspace.getUser().getDisplayName(), 
					gitEmail.getValue(), cloneInfo, workspace.getCommitHash(), workspace.getBranch(), 
					settingService.getSystemSetting().getServerUrl());

			var pinnedServerAddress = workspace.getServerAddress();
			var pinnedAgentId = workspace.getAgent() != null ? workspace.getAgent().getId() : null;

			return executorService.submit(() -> {
				logService.addLogger(token, workspaceLogger);
				try {
					var taskFuture = provisioner.submitTask(pinnedServerAddress, pinnedAgentId, newRunWorkspaceTask(context), workspaceLogger);
					workspaceTaskFutures.put(workspaceId, taskFuture);
					try {
						taskFuture.get();
					} finally {
						workspaceTaskFutures.remove(workspaceId);
					}
				} finally {
					logService.removeLogger(token);
				}
				return null;
			});
		} finally {
			Workspace.pop();
		}
	}

	private void markWorkspaceInactive(Workspace workspace) {
		workspace.setStatus(Workspace.Status.INACTIVE);
		workspace.setInactiveDate(new Date());
		update(workspace);
		logService.flush(workspace.getLoggingSupport());
		listenerRegistry.post(new WorkspaceInactive(workspace));
	}

	@Override
	public File getLogFile(Long projectId, Long workspaceNumber) {
		var logDir = projectService.getSubDir(projectId, WORKSPACE_LOGS_DIR);
		return new File(logDir, workspaceNumber.toString());
	}

	@Sessional
	@Override
	public String openShell(Workspace workspace, String label, @Nullable String command,
							 @Nullable ShellOutputCallback outputCallback) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			throw new ExplicitException("Workspace server not found");
		var shellId = clusterService.runOnServer(server, new ClusterTask<String>() {

			@Override
			public String call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				if (runtime == null)
					throw new ExplicitException("Workspace runtime not found");
				var shellIdRef = new AtomicReference<String>(null);
				var shellReady = new CompletableFuture<String>();
				var firstOutputSeen = new AtomicBoolean(false);
				shellIdRef.set(runtime.openShell(new Terminal() {

					@Override
					public void onShellOutput(String base64Data) {
						var shellId = shellIdRef.get();
						if (shellId != null) {
							// Use runOnAllServers instead of submitToAllServers to preserve output order
							clusterService.runOnAllServers(newHandleShellOutputTask(workspaceId, shellId, base64Data));
						}
						if (outputCallback != null) {
							try {
								outputCallback.onOutput(base64Data);
							} catch (Throwable e) {
								logger.error("Error calling shell output callback", e);
							}
						}

						if (command != null && firstOutputSeen.compareAndSet(false, true)) {
							shellReady.thenAccept(it -> {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
								runtime.writeShellStdin(it, command + "\n");			
							});
						}
					}
		
					@Override
					public void onShellExit() {
						var shellId = shellIdRef.get();
						if (shellId != null)
							clusterService.submitToAllServers(newHandleShellStopTask(workspaceId, shellId, false));
					}
		
				}, label));
				var shellId = shellIdRef.get();
				shellReady.complete(shellId);
				return shellId;
			}

		});
		listenerRegistry.post(new WorkspaceTerminalOpened(workspace));
		return shellId;
	}

	@Sessional
	@Override
	public void onOpen(IWebSocketConnection connection, Workspace workspace, String shellId) {
		var state = new OutputState(workspace.getId(), shellId);
		workspaceConnections.put(connection, state);

		var replayBuffer = terminalReplayBuffers.get(getTerminalKey(workspace.getId(), shellId));
		if (replayBuffer != null) {
			var chunks = replayBuffer.snapshot();
			if (!chunks.isEmpty()) {
				try {
					connection.sendMessage("TERMINAL_REPLAY_START");
					for (var base64Data: chunks)
						connection.sendMessage("SHELL_OUTPUT:" + base64Data);
					connection.sendMessage("TERMINAL_REPLAY_END");
				} catch (Exception e) {
					logger.warn("Error replaying terminal output", e);
				}
			}
		}
		var queuedChunks = state.markReplayCompletedAndGetQueuedChunks();
		if (!queuedChunks.isEmpty()) {
			try {
				for (var base64Data: queuedChunks)
					connection.sendMessage("SHELL_OUTPUT:" + base64Data);
			} catch (Exception e) {
				logger.warn("Error flushing queued terminal output", e);
			}
		}
	}

	@Override
	public void onClose(IWebSocketConnection connection) {
		workspaceConnections.remove(connection);
	}

	@Sessional
	@Override
	public void onMessage(Workspace workspace, String shellId, String message) {
		if (message.startsWith("SHELL_INPUT:") || message.startsWith("TERMINAL_RESIZE:")) {
			var workspaceId = workspace.getId();			
			var server = workspaceServers.get(workspaceId);
			if (server == null)
				return;

			clusterService.runOnServer(server, () -> {
				try {
					var runtime = workspaceRuntimes.get(workspaceId);
					if (runtime != null) {			
						if (message.startsWith("SHELL_INPUT:")) {
							runtime.writeShellStdin(shellId, message.substring("SHELL_INPUT:".length()));
						} else {
							var size = message.substring("TERMINAL_RESIZE:".length()).split(",");
							runtime.resizeShell(shellId, Integer.parseInt(size[0]), Integer.parseInt(size[1]));
						}
					}
				} catch (Throwable t) {
					logger.error("Error handling terminal message", t);
				}
				return null;
			});	
		}
	}

	@Sessional
	@Override
	public Map<String, String> getShellLabels(Workspace workspace) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			return Collections.emptyMap();

		return clusterService.runOnServer(server, new ClusterTask<Map<String, String>>() {

			@Override
			public Map<String, String> call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				return runtime != null ? runtime.getShellLabels() : new HashMap<>();
			}
		});
	}

	@Sessional
	@Override
	public Map<Integer, Integer> getPortMappings(Workspace workspace) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			return Collections.emptyMap();

		return clusterService.runOnServer(server, new ClusterTask<Map<Integer, Integer>>() {

			@Override
			public Map<Integer, Integer> call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				return runtime != null ? runtime.getPortMappings() : new HashMap<>();
			}
		});
	}

	@Sessional
	@Override
	public String getPortHost(Workspace workspace) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			throw new ExplicitException("Workspace server not found");

		return clusterService.runOnServer(server, new ClusterTask<String>() {

			@Override
			public String call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				if (runtime == null)
					throw new ExplicitException("Workspace runtime not found");
				return runtime.getPortHost();
			}
		});
	}

	private void terminateShell(Long workspaceId, String shellId) {
		var runtime = workspaceRuntimes.get(workspaceId);
		if (runtime != null)
			runtime.terminateShell(shellId);
		clusterService.submitToAllServers(newHandleShellStopTask(workspaceId, shellId, true));		
	}

	@Sessional
	@Override
	public void terminateShell(Workspace workspace, String shellId) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			return;

		clusterService.runOnServer(server, new ClusterTask<Void>() {

			@Override
			public Void call() throws Exception {
				terminateShell(workspaceId, shellId);
				return null;
			}

		});
	}

	private String getTerminalKey(Long workspaceId, String shellId) {
		return workspaceId + ":" + shellId;
	}

	@Override
	public WorkspaceContext getWorkspaceContext(String token, boolean mustExist) {
		var contextMap = clusterService.runOnAllServers(() -> workspaceContexts.get(token));
		var context = contextMap.values().stream()
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
		if (mustExist && context == null)
			throw new ExplicitException("No workspace context found for specified token");
		return context;
	}

	@Sessional
	@Override
	public GitExecutionResult executeGitCommand(Workspace workspace, String[] gitArgs) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			throw new ExplicitException("Workspace server not found");

		return clusterService.runOnServer(server, () -> {
			var runtime = workspaceRuntimes.get(workspaceId);
			if (runtime != null) {
				return runtime.executeGitCommand(gitArgs);
			} else {
				return new GitExecutionResult(new byte[0], "Workspace is not active".getBytes(UTF_8), 1);
			}
		});
	}

	@Sessional
	@Override
	public FileData readFileData(Workspace workspace, String path) {
		var workspaceId = workspace.getId();
		var server = workspaceServers.get(workspaceId);
		if (server == null)
			return null;
		
		return clusterService.runOnServer(server, () -> {
			var runtime = workspaceRuntimes.get(workspaceId);
			if (runtime != null)
				return runtime.readFileData(path);
			else
				return null;
		});
	}

	private Collection<Long> getActiveWorkspaceIds() {
		var activeWorkspaceIds = new HashSet<Long>();
		for (var value: clusterService.runOnAllServers(() -> workspaceContexts.values().stream().map(WorkspaceContext::getWorkspaceId).collect(Collectors.toSet())).values()) {
			activeWorkspaceIds.addAll(value);
		}
		return activeWorkspaceIds;
	}

	@Transactional
	@Override
	public void runPrompt(User ai, Project project, ObjectId commitId, String branch, String prompt, TaskFailedCallback taskFailedCallback) {
		WorkspaceSpec applicableSpec = null;
		for (var spec : project.getWorkspaceSpecs()) {
			if (spec.getTaskAutomation() != null) {
				var applicableUsers = spec.getTaskAutomation().getApplicableAis();
				if (applicableUsers.isEmpty() || applicableUsers.contains(ai.getName())) {
					applicableSpec = spec;
					break;
				}
			}
		}
		if (applicableSpec != null) {
			final WorkspaceSpec finalApplicableSpec = applicableSpec;
			var workspaceId = create(ai, project, commitId, branch, applicableSpec.getName(), true).getId();
			
			transactionService.runAfterCommit(() -> {
				executorService.execute(() -> {
					while (true) {					
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {							
						}
						if (sessionService.call(() -> {
							var workspace = get(workspaceId);
							if (workspace == null) {								
								logger.warn("AI task ignored as task workspace no longer exists");
								return true;
							}

							var workspaceReference = workspace.getReference().toString(project);
							if (workspace.getStatus() == Workspace.Status.ACTIVE) {
								String fullPrompt;
								if (ai.getAiSetting().getSystemPrompt() != null)
									fullPrompt = "Rules:\n" + ai.getAiSetting().getSystemPrompt() + "\n\nUser:\n" + prompt;
								else
									fullPrompt = prompt;
								
								var command = finalApplicableSpec.getShell().decorateRunPromptCommand(
										finalApplicableSpec.getTaskAutomation().getRunTaskCmd(), fullPrompt.trim(),
										RUN_PROMPT_SUCCESS_MARKER, RUN_PROMPT_FAILURE_MARKER);
								var buffer = new StringBuilder();
								var commandCompleted = new AtomicBoolean(false);
								openShell(workspace, "Terminal", command, base64Data -> {
									if (!commandCompleted.get()) synchronized (buffer) {
										try {
											buffer.append(new String(Base64.getDecoder().decode(base64Data), UTF_8));
										} catch (IllegalArgumentException e) {
											logger.warn("Unable to decode shell output", e);
											return;
										}
										if (buffer.indexOf(RUN_PROMPT_SUCCESS_MARKER) != -1) {											
											commandCompleted.set(true);
											if (finalApplicableSpec.getTaskAutomation().isDeleteWorkspaceIfSucceeded()) 
												delete(load(workspaceId));
										} else if (buffer.indexOf(RUN_PROMPT_FAILURE_MARKER) != -1) {
											commandCompleted.set(true);
											sessionService.run(() -> {
												taskFailedCallback.onTaskFailed(workspaceReference);
											});
										} else if (buffer.length() > 1024) {
											buffer.delete(0, buffer.length() - 1024);
										}
									}
								});		
								return true;
							} else if (workspace.getStatus() == Workspace.Status.INACTIVE) {
								sessionService.run(() -> {
									taskFailedCallback.onTaskFailed(workspaceReference);
								});
								return true;
							} else {
								return false;
							}
						})) {
							break;
						}
					}
				});
			});
		} else {
			throw new ExplicitException("I need to create workspace to do the job, but no applicable workspace spec found");
		}
	}

	private ClusterTask<Void> newRunWorkspaceTask(WorkspaceContext context) {
		return new ClusterTask<Void>() {
		
			private void run(WorkspaceContext context, TaskLogger workspaceLogger) {
				var workspaceId = context.getWorkspaceId();
				transactionService.run(() -> {
					var workspace = load(workspaceId);
					if (!Objects.equals(workspace.getProvisionerName(), context.getProvisioner().getName())) {
						workspace.setProvisionerName(context.getProvisioner().getName());
						update(workspace);
					}
				});
	
				var runtime = context.getProvisioner().provision(context, workspaceLogger);
				workspaceRuntimes.put(workspaceId, runtime);
				try {
					transactionService.run(() -> {
						var workspace = load(workspaceId);
						workspace.setStatus(Workspace.Status.ACTIVE);
						workspace.setActiveDate(new Date());
						if (!workspace.isForTaskAutomation()) {
							var firstShortcut = context.getSpec().getShortcutConfigs().stream().findFirst().orElse(null);
							if (firstShortcut != null) 
								openShell(workspace, firstShortcut.getName(), firstShortcut.getCommand(), null);
							else 
								openShell(workspace, "Terminal", null, null);
						}
						update(workspace);
						listenerRegistry.post(new WorkspaceActive(workspace));
					});
					workspaceLogger.success("Workspace provisioned and is active now");
	
					runtime.await();
				} finally {
					workspaceRuntimes.remove(workspaceId);
				}
			}
	
			@Override
			public Void call() throws Exception {
				String workspaceToken = context.getToken();
				while (thread == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
	
				workspaceContexts.put(workspaceToken, context);
				var workspaceId = context.getWorkspaceId();
				workspaceServers.put(workspaceId, clusterService.getLocalServerAddress());
				try {
					TaskLogger workspaceLogger = logService.getLogger(workspaceToken);
					if (workspaceLogger == null) {
						var activeServer = projectService.getActiveServer(context.getProjectId(), true);
						workspaceLogger = new ServerLogger(activeServer, workspaceToken);
						logService.addLogger(workspaceToken, workspaceLogger);
						try {
							run(context, workspaceLogger);
						} finally {
							logService.removeLogger(workspaceToken);
						}
					} else {
						run(context, workspaceLogger);
					}
				} finally {
					workspaceServers.remove(workspaceId);
					workspaceContexts.remove(workspaceToken);
				}
	
				return null;
			}
	
		};
	}

	private ClusterTask<Void> newHandleShellOutputTask(Long workspaceId, String shellId, String base64Data) {
		return new ClusterTask<Void>() {

			@Override
			public Void call() throws Exception {
				terminalReplayBuffers
					.computeIfAbsent(getTerminalKey(workspaceId, shellId), key -> new ReplayBuffer())
					.append(base64Data);
	
				var connections = workspaceConnections;
	
				for (var entry : connections.entrySet()) {
					var connection = entry.getKey();
					var state = entry.getValue();
					if (state.matches(workspaceId, shellId) && !state.queueIfReplaying(base64Data) && connection.isOpen()) 
						connection.sendMessage("SHELL_OUTPUT:" + base64Data);
				}
				return null;
			}
	
		};
	}

	private ClusterTask<Void> newHandleShellStopTask(Long workspaceId, String shellId, boolean terminated) {
		return new ClusterTask<Void>() {

			@Override
			public Void call() throws Exception {
				var connections = workspaceConnections;
				for (var entry : connections.entrySet()) {
					var connection = entry.getKey();
					var shellKey = entry.getValue();
					if (shellKey.matches(workspaceId, shellId)) {
						if (terminated) {
							connection.sendMessage("SHELL_OUTPUT:" + AgentUtils.encodeBase64Error("Shell terminated"));
						} else {
							sessionService.run(() -> {
								connection.sendMessage(new ShellExit());
							});
						}
					}
				}
				terminalReplayBuffers.remove(getTerminalKey(workspaceId, shellId));
				return null;
			}
		};
	}

	private static class OutputState {

		private final Long workspaceId;

		private final String shellId;

		private final ArrayDeque<String> queuedChunks = new ArrayDeque<>();

		private boolean replaying = true;

		public OutputState(Long workspaceId, String shellId) {
			this.workspaceId = workspaceId;
			this.shellId = shellId;
		}

		public boolean matches(Long workspaceId, String shellId) {
			return this.workspaceId.equals(workspaceId) && this.shellId.equals(shellId);
		}

		public synchronized boolean queueIfReplaying(String base64Data) {
			if (replaying) {
				queuedChunks.addLast(base64Data);
				return true;
			} else {
				return false;
			}
		}

		public synchronized List<String> markReplayCompletedAndGetQueuedChunks() {
			replaying = false;
			var chunks = new ArrayList<>(queuedChunks);
			queuedChunks.clear();
			return chunks;
		}

	}

	private static class ReplayBuffer {

		private static final int MAX_TOTAL_CHARS = 16 * 1024;

		private final ArrayDeque<String> chunks = new ArrayDeque<>();

		private int totalChars;

		public synchronized void append(String base64Data) {
			chunks.addLast(base64Data);
			totalChars += base64Data.length();
			while (totalChars > MAX_TOTAL_CHARS && chunks.size() > 1) {
				totalChars -= chunks.removeFirst().length();
			}
		}

		public synchronized List<String> snapshot() {
			return new ArrayList<>(chunks);
		}

	}

}
