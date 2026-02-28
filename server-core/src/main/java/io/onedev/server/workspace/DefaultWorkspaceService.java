package io.onedev.server.workspace;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static io.onedev.server.util.DirectoryVersionUtils.readVersion;
import static io.onedev.server.util.DirectoryVersionUtils.writeVersion;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.jspecify.annotations.Nullable;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.k8shelper.DefaultCloneInfo;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.EnvVar;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.workspace.WorkspaceActive;
import io.onedev.server.event.project.workspace.WorkspaceCreated;
import io.onedev.server.event.project.workspace.WorkspaceError;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotFoundException;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitTask;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.logging.LogService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.persistence.SequenceGenerator;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.CodePushAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.WriteCode;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UrlService;
import io.onedev.server.service.impl.BaseEntityService;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.FileData;
import io.onedev.server.util.ProjectWorkspaceStatusStat;
import io.onedev.server.util.concurrent.DynamicSemaphore;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.component.terminal.ShellExit;
import oshi.SystemInfo;

@Singleton
public class DefaultWorkspaceService extends BaseEntityService<Workspace> 
		implements WorkspaceService, Runnable, SchedulableTask, CodePullAuthorizationSource, CodePushAuthorizationSource, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultWorkspaceService.class);

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds

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

	private SequenceGenerator numberGenerator;

	private final Map<String, WorkspaceContext> workspaceContexts = new ConcurrentHashMap<>();

	private final Map<Long, Future<Void>> workspaceFutures = new ConcurrentHashMap<>();

	private final Map<Long, WorkspaceRuntime> workspaceRuntimes = new ConcurrentHashMap<>();

	private final Map<IWebSocketConnection, OutputState> workspaceConnections = new ConcurrentHashMap<>();

	private final Map<String, ReplayBuffer> terminalReplayBuffers = new ConcurrentHashMap<>();

	private final Map<String, DynamicSemaphore> workspaceSemaphores = new ConcurrentHashMap<>();

	private volatile Thread thread;

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
	public void create(Workspace workspace) {
		Preconditions.checkArgument(workspace.isNew());
		workspace.setNumberScope(workspace.getProject().getForkRoot());
		workspace.setNumber(getNumberGenerator().getNextSequence(workspace.getNumberScope()));
		dao.persist(workspace);

		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		projectService.runOnActiveServer(projectId, new ClusterTask<Void>() {

			@Override
			public Void call() throws Exception {
				FileUtils.cleanDir(getWorkspaceDir(projectId, workspaceNumber));
				return null;
			}

		});
		listenerRegistry.post(new WorkspaceCreated(workspace));
	}

	@Transactional
	@Override
	public void update(Workspace workspace) {
		Preconditions.checkState(!workspace.isNew());
		dao.persist(workspace);
	}

	@Sessional
	@Listen
	public void on(WorkspaceError event) {
		logService.flush(event.getWorkspace().getLoggingSupport());
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		getNumberGenerator().removeNextSequence(project);
		}
	}

	private Collection<Predicate> getPredicates(Subject subject, @Nullable Project project,
                                                From<Workspace, Workspace> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get(Workspace.PROP_PROJECT), project));
		} else if (!SecurityUtils.isAdministrator(subject)) {
			Collection<Project> projects = SecurityUtils.getAuthorizedProjects(subject, new WriteCode());
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
	public void delete(Workspace workspace) {
		var loggingSupport = workspace.getLoggingSupport();
		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		projectService.runOnActiveServer(projectId, new ClusterTask<Void>() {

			@Override
			public Void call() throws Exception {
				logService.flush(loggingSupport);
				FileUtils.deleteDir(getWorkspaceDir(projectId, workspaceNumber));
				return null;
			}

		});
		dao.remove(workspace);
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
		if (copy != null) {
			try {
				copy.join();
			} catch (InterruptedException ignored) {
			}
		}
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
			if (thread == null) {
				for (var future: workspaceFutures.values()) 
					future.cancel(true);
			}
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
								while (thread == null)
									Thread.sleep(1000);	
								transactionService.run(() -> {
									for (var workspaceId : workspaceIdsOfServer) {
										var workspace = load(workspaceId);
										if (workspace.getStatus() == Status.PENDING) {
											var future = workspaceFutures.get(workspace.getId());
											if (future == null && thread != null) {
												try {
													workspaceFutures.put(workspace.getId(), provision(workspace));
												} catch (Throwable t) {
													log(logService.newLogger(workspace.getLoggingSupport()), t);
													markWorkspaceError(workspace);
												}
											}
										} else if (workspace.getStatus() == Status.ACTIVE) {
											if (workspaceFutures.get(workspace.getId()) == null) {
												workspace.setActiveDate(null);
												workspace.setStatus(Status.PENDING);
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
					for (var future : futures) {
						try {
							future.get();
						} catch (InterruptedException | ExecutionException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
				sessionService.run(() -> {
					for (var it = workspaceFutures.entrySet().iterator(); it.hasNext(); ) {
						var entry = it.next();
						var workspace = get(entry.getKey());
						var future = entry.getValue();
						if (workspace == null 
								|| workspace.getStatus() == Status.ERROR 
								|| !localServer.equals(projectService.getActiveServer(workspace.getProject().getId(), false))) {
							future.cancel(true);
							it.remove();
						} else if (future.isDone()) {
							var logger = logService.newLogger(workspace.getLoggingSupport());
							try {
								future.get();
								logger.error("Workspace runtime stopped for unknown reason");
							} catch (Throwable t) {
								log(logger, t);
								markWorkspaceError(workspace);
							} finally {
								it.remove();
							}
						}
					}
				});
				Thread.sleep(CHECK_INTERVAL);
			} catch (Throwable t) {
				if (ExceptionUtils.find(t, ServerNotFoundException.class) == null)
					logger.error("Error checking workspaces", t);
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
			if (runtime == null || !runtime.getShellIds().contains(shellId)) 
				it.remove();
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatHourlyForever();
	}

	private boolean isApplicable(Workspace workspace, WorkspaceProvisioner provisioner) {
		return provisioner.getApplicableProjects() == null 
			|| WildcardUtils.matchPath(provisioner.getApplicableProjects(), workspace.getProject().getPath());
	}

	private WorkspaceProvisioner getProvisioner(Workspace workspace, TaskLogger logger) {
		if (!settingService.getWorkspaceProvisioners().isEmpty()) {
			for (var provisioner : settingService.getWorkspaceProvisioners()) {
				if (provisioner.isEnabled() && isApplicable(workspace, provisioner))
					return provisioner;
			}
			throw new ExplicitException("No applicable workspace provisioner");
		} else {
			throw new ExplicitException("No applicable provisioner discovered for current workspace");
		}
	}

	private Future<Void> provision(Workspace workspace) {
		var workspaceSpec = workspace.getProject().getHierarchyWorkspaceSpecs().stream()
				.filter(s -> s.getName().equals(workspace.getSpecName()))
				.findFirst()
				.orElseThrow(() -> new ExplicitException("Spec not found in workspace project hierarchy"));

		String token = workspace.getToken();

		var logger = logService.newLogger(workspace.getLoggingSupport());
		var provisioner = getProvisioner(workspace, logger);

		var project = workspace.getProject();
		var projectId = project.getId();
		var projectPath = project.getPath();
		var projectGitDir = projectService.getGitDir(project.getId()).getAbsolutePath();
		var workspaceId = workspace.getId();
		var workspaceNumber = workspace.getNumber();

		var envVars = workspaceSpec.getEnvVars().stream().collect(toMap(EnvVar::getName, EnvVar::getValue));
		var userConfigs = workspaceSpec.getUserConfigs().stream()
				.collect(toMap(uc -> uc.getPath(), uc -> uc.getContent()));

		var gitEmail = workspace.getUser().getGitEmailAddress();
		if (gitEmail == null)
			throw new ExplicitException("No email address for git operations configured");
		
		var cloneInfo = new DefaultCloneInfo(urlService.cloneUrlFor(workspace.getProject(), false), token);		
		var branch = project.getDefaultBranch() != null? workspace.getBranch(): null;
		var context = new WorkspaceContext(token, projectId, projectPath, projectGitDir, workspaceId, 
			workspaceNumber, workspaceSpec.getShell(), envVars, cloneInfo, workspaceSpec.isRetrieveLfs(), 
			branch, workspace.getUser().getDisplayName(), gitEmail.getValue(), userConfigs);
				
		var concurrency = provisioner.getConcurrency();
		if (concurrency == null)
			concurrency = Math.max(1, new SystemInfo().getHardware().getProcessor().getLogicalProcessorCount());
		var semaphore = workspaceSemaphores.computeIfAbsent(provisioner.getName(), k -> new DynamicSemaphore());
		semaphore.setMaxPermits(concurrency);

		logger.log("Waiting for resource allocation...");

		return executorService.submit(() -> {
			workspaceContexts.put(token, context);
			semaphore.acquire();
			try {
				var runtime = provisioner.provision(context, logger);
				workspaceRuntimes.put(context.getWorkspaceId(), runtime);
				try {
					transactionService.run(() -> {
						var innerWorkspace = load(workspaceId);
						innerWorkspace.setStatus(Workspace.Status.ACTIVE);
						innerWorkspace.setActiveDate(new Date());
						update(innerWorkspace);
						listenerRegistry.post(new WorkspaceActive(innerWorkspace));
					});
					logger.success("Workspace provisioned and is active now");

					runtime.await();
					return null;
				} finally {
					for (var shellId : runtime.getShellIds())
						terminateShell(workspaceId, shellId);
					workspaceRuntimes.remove(workspaceId);
				}
			} finally {
				semaphore.release();
				workspaceContexts.remove(token);
			}
		});
	}

	private void markWorkspaceError(Workspace workspace) {
		workspace.setStatus(Workspace.Status.ERROR);
		workspace.setErrorDate(new Date());
		update(workspace);
		listenerRegistry.post(new WorkspaceError(workspace));
	}

	private void log(TaskLogger logger, Throwable throwable) {
		var explicitException = ExceptionUtils.find(throwable, ExplicitException.class);
		if (explicitException != null)
			logger.error(explicitException.getMessage());
		else
			logger.error("Error processing workspace", throwable);
	}

	@Override
	public File getWorkspaceDir(Long projectId, Long workspaceNumber) {
		return projectService.getSubDir(projectId, Workspace.getProjectRelativeDirPath(workspaceNumber));
	}

	@Sessional
	@Override
	public String openShell(Workspace workspace) {
		var projectId = workspace.getProject().getId();
		var workspaceId = workspace.getId();

		return projectService.runOnActiveServer(projectId, new ClusterTask<String>() {

			@Override
			public String call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				if (runtime == null)
					throw new ExplicitException("Workspace runtime not found");
				var shellIdRef = new AtomicReference<String>(null);
				shellIdRef.set(runtime.openShell(new Terminal() {
		
					@Override
					public void onShellOutput(String base64Data) {
						var shellId = shellIdRef.get();
						if (shellId != null) 
							// Use runOnAllServers instead of submitToAllServers to preserve output order
							clusterService.runOnAllServers(new HandleShellOutputTask(workspaceId, shellId, base64Data));
					}
		
					@Override
					public void onShellExit() {
						var shellId = shellIdRef.get();
						if (shellId != null)
							clusterService.submitToAllServers(new HandleShellStopTask(workspaceId, shellId, false));
					}
		
				}));
				return shellIdRef.get();
			}

		});

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
	public void onMessage(IWebSocketConnection connection, Workspace workspace, 
					String shellId, String message) {
		if (message.startsWith("SHELL_INPUT:") || message.startsWith("TERMINAL_RESIZE:")) {
			var workspaceId = workspace.getId();
			var projectId = workspace.getProject().getId();
			projectService.runOnActiveServer(projectId, () -> {
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
	public List<String> getShellIds(Workspace workspace) {
		var projectId = workspace.getProject().getId();
		var workspaceId = workspace.getId();

		return projectService.runOnActiveServer(projectId, new ClusterTask<List<String>>() {

			@Override
			public List<String> call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				return runtime != null? runtime.getShellIds(): new ArrayList<>();
			}
		});

	}

	private void terminateShell(Long workspaceId, String shellId) {
		var runtime = workspaceRuntimes.get(workspaceId);
		if (runtime != null)
			runtime.terminateShell(shellId);
		clusterService.submitToAllServers(new HandleShellStopTask(workspaceId, shellId, true));		
	}

	@Sessional
	@Override
	public void terminateShell(Workspace workspace, String shellId) {
		var workspaceId = workspace.getId();
		var projectId = workspace.getProject().getId();

		projectService.runOnActiveServer(projectId, new ClusterTask<Void>() {

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

	private boolean isAuthorized(HttpServletRequest request, Project project) {
		String token = SecurityUtils.getBearerToken(request);
		if (token != null) {
			var context = projectService.runOnActiveServer(project.getId(), () -> {
				return workspaceContexts.get(token);
			});
			if (context != null)
				return context.getProjectId().equals(project.getId());
		}
		return false;
	}

	@Override
	public boolean hasLfsObjects(Long projectId, Long workspaceNumber) {
		var workDir = Workspace.getWorkDir(projectId, workspaceNumber);
		var gitDir = new File(workDir, ".git");
		return new File(gitDir, "lfs/objects").exists();
	}
	
	@Override
	public boolean canPullCode(HttpServletRequest request, Project project) {
		return isAuthorized(request, project);
	}

	@Override
	public boolean canPushCode(HttpServletRequest request, Project project) {
		return isAuthorized(request, project);
	}

	@Override
	public void onPostCommit(WorkspaceContext context) {
		var workspaceDir = getWorkspaceDir(context.getProjectId(), context.getWorkspaceNumber());
		projectService.directoryModified(context.getProjectId(), workspaceDir);
	}

	@Override
	public void syncWorkspaces(Long projectId, String activeServer) {
		projectService.syncDirectory(projectId, Workspace.WORKSPACES_DIR, (suffix) -> {
			projectService.syncDirectory(projectId, Workspace.WORKSPACES_DIR + "/" + suffix, (workspaceNumberStr) -> {
				syncWorkspace(projectId, Long.valueOf(workspaceNumberStr), activeServer);
			}, activeServer);
		}, activeServer);
	}

	private void syncWorkspace(Long projectId, Long workspaceNumber, String activeServer) {
		var workDir = Workspace.getWorkDir(projectId, workspaceNumber);
		var workspaceDir = workDir.getParentFile();
		var gitDir = new File(workDir, ".git");

		var remoteWorkspaceVersion = clusterService.runOnServer(activeServer, () -> {
			var remoteWorkspaceDir = getWorkspaceDir(projectId, workspaceNumber);
			return readVersion(remoteWorkspaceDir);
		});
		var workspaceVersion = readVersion(workspaceDir);
		if (workspaceVersion < remoteWorkspaceVersion) {
			CommandUtils.callWithClusterCredential(new GitTask<Void>() {
				@Override
				public Void call(Commandline git) throws IOException {
					var projectPath = projectService.findFacadeById(projectId).getPath();
					var cloneUrl = clusterService.getServerUrl(activeServer)
							+ "/" + projectPath + "/" + GIT_PREFIX + workspaceNumber;
					var debugLogger = new LineConsumer() {
						@Override
						public void consume(String line) {
							logger.debug(line);
						}
					};
					var errorLogger = new LineConsumer() {
						@Override
						public void consume(String line) {
							logger.error(line);
						}
					};

					if (clusterService.runOnServer(activeServer, new HasLfsObjectsTask(projectId, workspaceNumber))) 
						KubernetesHelper.installGitLfs(git, debugLogger, errorLogger);

					if (!gitDir.exists()) {
						FileUtils.deleteDir(workDir);
						git.addArgs("clone", cloneUrl, workDir.getAbsolutePath());
						git.execute(debugLogger, new LineConsumer() {
							@Override
							public void consume(String line) {
								if (!line.startsWith("Cloning into"))
									logger.error(line);
								else
									logger.debug(line);
							}
						}).checkReturnCode();
						git.clearArgs();
					} else {
						git.addArgs("ls-remote", "--symref", cloneUrl, "HEAD");
						var branchRef = new AtomicReference<String>();
						git.execute(new LineConsumer() {
							@Override
							public void consume(String line) {
								if (line.startsWith("ref: refs/heads/"))
									branchRef.set(line.substring("ref: refs/heads/".length()).split("\\t")[0]);
							}
						}, errorLogger).checkReturnCode();
						git.clearArgs();

						var branch = branchRef.get();
						if (branch != null) {
							git.workingDir(workDir);

							git.addArgs("remote", "set-url", "origin", cloneUrl);
							git.execute(debugLogger, errorLogger).checkReturnCode();
							git.clearArgs();

							git.addArgs("fetch", "origin", "refs/heads/" + branch);
							git.execute(debugLogger, new LineConsumer() {
								@Override
								public void consume(String line) {
									if (!line.startsWith("From") && !line.contains("->"))
										logger.error(line);
									else
										logger.debug(line);
								}
							}).checkReturnCode();
							git.clearArgs();

							git.addArgs("checkout", "-f", "-B", branch, "FETCH_HEAD");
							git.execute(debugLogger, new LineConsumer() {
								@Override
								public void consume(String line) {
									if (!line.startsWith("Reset branch") && !line.startsWith("Switched to"))
										logger.error(line);
								}
							}).checkReturnCode();
							git.clearArgs();
						}
					}
					return null;
				}
			});

			writeVersion(workspaceDir, remoteWorkspaceVersion);
		}
	}

	@Sessional
	@Override
	public GitExecutionResult executeGitCommand(Workspace workspace, String[] gitArgs) {
		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		var workspaceToken = workspace.getToken();
		return projectService.runOnActiveServer(projectId, () -> {
			var git = CommandUtils.newGit();
			var workDir = Workspace.getWorkDir(projectId, workspaceNumber);					
			git.workingDir(workDir);
			git.environments().putAll(HookUtils.getWorkspacePostCommitHookEnvs(workspaceToken));
			git.arguments(Arrays.asList(gitArgs));

			var stdoutStream = new ByteArrayOutputStream();
			var stderrStream = new ByteArrayOutputStream();
			var returnCode = git.execute(stdoutStream, stderrStream).getReturnCode();
			return new GitExecutionResult(stdoutStream.toByteArray(), stderrStream.toByteArray(), returnCode);
		});
	}

	@Sessional
	@Override
	public FileData readFileData(Workspace workspace, String path) {
		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		return projectService.runOnActiveServer(projectId, () -> {
			var workDir = Workspace.getWorkDir(projectId, workspaceNumber);
			try {
				return FileData.from(new File(workDir, path));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private static class HandleShellOutputTask implements ClusterTask<Void> {

		private static final long serialVersionUID = 1L;

		private final Long workspaceId;

		private final String shellId;

		private final String base64Data;

		public HandleShellOutputTask(Long workspaceId, String shellId, String base64Data) {
			this.workspaceId = workspaceId;
			this.shellId = shellId;
			this.base64Data = base64Data;
		}

		@Override
		public Void call() throws Exception {
			var workspaceService = OneDev.getInstance(DefaultWorkspaceService.class);
			workspaceService.terminalReplayBuffers
				.computeIfAbsent(workspaceService.getTerminalKey(workspaceId, shellId), key -> new ReplayBuffer())
				.append(base64Data);

			var connections = workspaceService.workspaceConnections;

			for (var entry : connections.entrySet()) {
				var connection = entry.getKey();
				var state = entry.getValue();
				if (state.matches(workspaceId, shellId) && !state.queueIfReplaying(base64Data))
					connection.sendMessage("SHELL_OUTPUT:" + base64Data);
			}
			return null;
		}

	}

	private static class HandleShellStopTask implements ClusterTask<Void> {

		private static final long serialVersionUID = 1L;

		private final Long workspaceId;

		private final String shellId;

		private final boolean terminated;

		/**
		 * @param workspaceId
		 * @param shellId
		 * @param terminated whether or not the shell is terminated by closing terminal tab
		 */
		public HandleShellStopTask(Long workspaceId, String shellId, boolean terminated) {
			this.workspaceId = workspaceId;
			this.shellId = shellId;
			this.terminated = terminated;
		}

		@Override
		public Void call() throws Exception {
			var workspaceService = OneDev.getInstance(DefaultWorkspaceService.class);

			var connections = workspaceService.workspaceConnections;
			for (var entry : connections.entrySet()) {
				var connection = entry.getKey();
				var state = entry.getValue();
				if (state.matches(workspaceId, shellId)) {
					if (terminated) {
						connection.sendMessage("SHELL_OUTPUT:" + AgentUtils.encodeBase64Error("Shell terminated"));
					} else {
						workspaceService.sessionService.run(() -> {
							connection.sendMessage(new ShellExit());
						});
					}
				}
			}
			workspaceService.terminalReplayBuffers.remove(workspaceService.getTerminalKey(workspaceId, shellId));
			return null;
		}

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

		private static final int MAX_TOTAL_CHARS = 512 * 1024;

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

	private static class HasLfsObjectsTask implements ClusterTask<Boolean> {

		private static final long serialVersionUID = 1L;

		private final Long projectId;

		private final Long workspaceNumber;
		
		public HasLfsObjectsTask(Long projectId, Long workspaceNumber) {
			this.projectId = projectId;
			this.workspaceNumber = workspaceNumber;
		}
		
		@Override
		public Boolean call() throws Exception {
			var workspaceService = OneDev.getInstance(DefaultWorkspaceService.class);
			return workspaceService.projectService.runOnActiveServer(projectId, () -> workspaceService.hasLfsObjects(projectId, workspaceNumber));
		}
		
	}
}
