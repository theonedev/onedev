package io.onedev.server.workspace;

import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
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
import io.onedev.k8shelper.DefaultCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.project.workspace.WorkspaceActive;
import io.onedev.server.event.project.workspace.WorkspaceCreated;
import io.onedev.server.event.project.workspace.WorkspaceEvent;
import io.onedev.server.event.project.workspace.WorkspaceInactive;
import io.onedev.server.event.project.workspace.WorkspacePending;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.ServerNotFoundException;
import io.onedev.server.logging.LogService;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
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
import io.onedev.server.util.interpolative.WorkspaceVariableInterpolator;
import io.onedev.server.web.component.terminal.ShellExit;
import io.onedev.server.web.editable.EditableUtils;
import oshi.SystemInfo;

@Singleton
public class DefaultWorkspaceService extends BaseEntityService<Workspace> 
		implements WorkspaceService, Runnable, SchedulableTask, CodePullAuthorizationSource, CodePushAuthorizationSource, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DefaultWorkspaceService.class);

	private static final int CHECK_INTERVAL = 5;

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
	
	// With tmux, we no longer need to replay the output. However we also send error messages 
	// (for instance when tmux is not installed) to terminal, so we still need to replay the 
	// output to ensure the error messages are displayed when websocket connection is 
	// established after shell reports error
	private final Map<String, ReplayBuffer> terminalReplayBuffers = new ConcurrentHashMap<>();

	private final Map<String, DynamicSemaphore> workspaceSemaphores = new ConcurrentHashMap<>();

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
	public void create(Workspace workspace) {
		Preconditions.checkArgument(workspace.isNew());
		workspace.setNumberScope(workspace.getProject().getForkRoot());
		workspace.setNumber(getNumberGenerator().getNextSequence(workspace.getNumberScope()));
		dao.persist(workspace);
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

			// record project server since workspace deletion can be called while
			// deleting a project
			var projectServer = projectService.getActiveServer(projectId, true);

			if (projectServer != null) {
				transactionService.runAfterCommit(() -> {
					clusterService.submitToServer(projectServer, (ClusterTask<Void>) () -> {
						try {
							checkImmediately = true;

							// Delete workspace directory after workspace runtime is removed to
							// give a chance for cache to be uploaded
							while (workspaceRuntimes.containsKey(workspaceId))
								Thread.sleep(1000);
							logService.flush(loggingSupport);

							var workspaceDir = getWorkspaceDir(projectId, workspaceNumber);
							FileUtils.deleteDir(workspaceDir);
							projectService.directoryModified(projectId, workspaceDir.getParentFile());
						} catch (Throwable t) {
							var message = "Error deleting workspace storage directory (project id: %d, workspace number: %d)"
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
	public void requestToReprovision(Workspace workspace) {
		Preconditions.checkState(workspace.getStatus() == Status.INACTIVE);
		workspace.setStatus(Status.PENDING);
		workspace.setInactiveDate(null);
		workspace.setActiveDate(null);
		var projectId = workspace.getProject().getId();
		var workspaceNumber = workspace.getNumber();
		projectService.runOnActiveServer(projectId, (ClusterTask<Void>) () -> {
			// Clear log file instead of delete as we may not have permission over the
			// workspace directory upon an abnormal shutdown
			var logFile = Workspace.getLogFile(projectId, workspaceNumber);
			if (logFile.exists())
				Files.newByteChannel(logFile.toPath(), WRITE, TRUNCATE_EXISTING).close();
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
													var logger = logService.newLogger(workspace.getLoggingSupport());
													var explicitException = ExceptionUtils.find(t, ExplicitException.class);
													if (explicitException != null)
														logger.error("Error provisioning workspace: " + explicitException.getMessage());
													else
														logger.error("Error provisioning workspace", t);
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
							var logger = logService.newLogger(workspace.getLoggingSupport());
							try {
								future.get();
								logger.error("Workspace stopped for unknown reason");
							} catch (Throwable t) {
								if (ExceptionUtils.find(t, CancellationException.class) != null) {
									logger.error("Workspace stopped due to server restart. You may reprovision the workspace to continue your work");
								} else if (ExceptionUtils.find(t, InterruptedException.class) == null) {
									var explicitException = ExceptionUtils.find(t, ExplicitException.class);
									if (explicitException != null)
										logger.error("Workspace stopped: " + explicitException.getMessage());
									else
										logger.error("Workspace stopped", t);
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
			List<WorkspaceProvisionerDiscoverer> discoverers = new ArrayList<>(
					OneDev.getExtensions(WorkspaceProvisionerDiscoverer.class));
			discoverers.sort(Comparator.comparing(WorkspaceProvisionerDiscoverer::getOrder));
			for (var discoverer : discoverers) {
				WorkspaceProvisioner provisioner = discoverer.discover();
				if (provisioner != null) {
					provisioner.setName("auto-discovered");
					if (isApplicable(workspace, spec, provisioner)) {
						logger.log("Discovered " + EditableUtils.getDisplayName(provisioner.getClass()).toLowerCase());
						return provisioner;
					}
				}
			}

			if (spec.isRunInContainer()) {
				throw new ExplicitException("""
					No applicable provisioner discovered for current workspace. \
					Please check if docker is installed on OneDev server""");
			} else {
				throw new ExplicitException("""
					No applicable provisioner discovered for current workspace. \
					Please add a shell provisioner in menu 'Administration / Workspace Provisioners' \
					with applicable projects configured properly""");
			}
		}
	}

	private Future<Void> provision(Workspace workspace) {
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

			var workspaceDir = getWorkspaceDir(projectId, workspaceNumber);
			FileUtils.createDir(workspaceDir);

			var logger = logService.newLogger(workspace.getLoggingSupport());
			var provisioner = getProvisioner(workspace, spec, logger);

			var gitEmail = workspace.getUser().getGitEmailAddress();
			if (gitEmail == null)
				throw new ExplicitException("No email address for git operations configured");
			
			var cloneInfo = new DefaultCloneInfo(urlService.cloneUrlFor(workspace.getProject(), false), token);		
			var branch = project.getDefaultBranch() != null? workspace.getBranch(): null;

			var context = new WorkspaceContext(spec, token, projectId, projectPath, projectGitDir, 
					workspaceId, workspaceNumber, workspace.getUser().getId(), workspace.getUser().getDisplayName(), 
					gitEmail.getValue(), cloneInfo, branch);
					
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
					} finally {
						for (var shellId : runtime.getShellLabels().keySet())
							terminateShell(workspaceId, shellId);
						workspaceRuntimes.remove(workspaceId);
					}
				} finally {
					semaphore.release();
					workspaceContexts.remove(token);
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
	public File getWorkspaceDir(Long projectId, Long workspaceNumber) {
		return projectService.getSubDir(projectId, Workspace.getProjectRelativeDirPath(workspaceNumber));
	}

	@Sessional
	@Override
	public String openShell(Workspace workspace, String label) {
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
		
				}, label));
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
	public Map<String, String> getShellLabels(Workspace workspace) {
		var projectId = workspace.getProject().getId();
		var workspaceId = workspace.getId();

		return projectService.runOnActiveServer(projectId, new ClusterTask<Map<String, String>>() {

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
		var projectId = workspace.getProject().getId();
		var workspaceId = workspace.getId();

		return projectService.runOnActiveServer(projectId, new ClusterTask<Map<Integer, Integer>>() {

			@Override
			public Map<Integer, Integer> call() throws Exception {
				var runtime = workspaceRuntimes.get(workspaceId);
				return runtime != null ? runtime.getPortMappings() : new HashMap<>();
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

	@Sessional
	@Override
	public GitExecutionResult executeGitCommand(Workspace workspace, String[] gitArgs) {
		var projectId = workspace.getProject().getId();
		var workspaceId = workspace.getId();
		return projectService.runOnActiveServer(projectId, () -> {
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
				var shellKey = entry.getValue();
				if (shellKey.matches(workspaceId, shellId)) {
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

		private static final int MAX_TOTAL_CHARS = 64 * 1024;

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
