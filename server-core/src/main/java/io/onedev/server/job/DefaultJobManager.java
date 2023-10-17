package io.onedev.server.job;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hazelcast.map.IMap;
import io.onedev.agent.job.FailedException;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.*;
import io.onedev.k8shelper.*;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecParseException;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.*;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.action.condition.ActionCondition;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.buildspec.job.retrycondition.RetryCondition;
import io.onedev.server.buildspec.job.retrycondition.RetryContext;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.job.trigger.ScheduleTrigger;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.buildspec.step.ServerSideStep;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.*;
import io.onedev.server.event.project.build.*;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.ServerJobLogger;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.*;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.code.CodeIndexManager;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.WebShell;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.web.editable.EditableStringTransformer;
import io.onedev.server.web.editable.EditableUtils;
import nl.altindag.ssl.SSLFactory;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;

@Singleton
public class DefaultJobManager implements JobManager, Runnable, CodePullAuthorizationSource, 
		Serializable, SchedulableTask {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds

	private static final Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);

	private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();

	private final Map<String, Collection<Thread>> serverStepThreads = new ConcurrentHashMap<>();

	private final Map<String, List<Action>> jobActions = new ConcurrentHashMap<>();

	private final Map<String, JobRunnable> jobRunnables = new ConcurrentHashMap<>();

	private final Map<Long, Collection<String>> projectTasks = new ConcurrentHashMap<>();

	private final Map<String, Shell> jobShells = new ConcurrentHashMap<>();

	private final Dao dao;
	
	private final ProjectManager projectManager;

	private final BuildManager buildManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final IssueManager issueManager;

	private final ListenerRegistry listenerRegistry;

	private final TransactionManager transactionManager;

	private final SessionManager sessionManager;

	private final LogManager logManager;

	private final UserManager userManager;

	private final SettingManager settingManager;

	private final ExecutorService executorService;

	private final BuildParamManager buildParamManager;

	private final TaskScheduler taskScheduler;

	private final Validator validator;

	private final ClusterManager clusterManager;

	private final CodeIndexManager codeIndexManager;
	
	private final GitService gitService;
	
	private final SSLFactory sslFactory;

	private volatile Thread thread;

	private final Map<String, JobContext> jobContexts = new ConcurrentHashMap<>();

	private volatile IMap<String, String> jobServers;

	private volatile Map<String, Collection<String>> allocatedCaches;
	
	private volatile String maintenanceTaskId;

	@Inject
	public DefaultJobManager(BuildManager buildManager, UserManager userManager, ListenerRegistry listenerRegistry,
							 SettingManager settingManager, TransactionManager transactionManager, LogManager logManager,
							 ExecutorService executorService, SessionManager sessionManager, BuildParamManager buildParamManager,
							 ProjectManager projectManager, Validator validator, TaskScheduler taskScheduler,
							 ClusterManager clusterManager, CodeIndexManager codeIndexManager, PullRequestManager pullRequestManager, 
							 IssueManager issueManager, GitService gitService, SSLFactory sslFactory, Dao dao) {
		this.dao = dao;
		this.settingManager = settingManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.logManager = logManager;
		this.executorService = executorService;
		this.sessionManager = sessionManager;
		this.buildParamManager = buildParamManager;
		this.projectManager = projectManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
		this.codeIndexManager = codeIndexManager;
		this.clusterManager = clusterManager;
		this.pullRequestManager = pullRequestManager;
		this.issueManager = issueManager;
		this.gitService = gitService;
		this.sslFactory = sslFactory;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(JobManager.class);
	}

	private void validateBuildSpec(Project project, ObjectId commitId, BuildSpec buildSpec) {
		Project.push(project);
		try {
			for (ConstraintViolation<?> violation : validator.validate(buildSpec)) {
				String message = String.format("Error validating build spec (project: %s, commit: %s, location: %s, message: %s)",
						project.getPath(), commitId.name(), violation.getPropertyPath(), violation.getMessage());
				throw new ExplicitException(message);
			}
		} finally {
			Project.pop();
		}
	}

	@Transactional
	@Override
	public Build submit(Project project, ObjectId commitId, String jobName, 
						Map<String, List<String>> paramMap, String pipeline,
						String refName, User submitter, @Nullable PullRequest request,
						@Nullable Issue issue, String reason) {
		Lock lock = LockUtils.getLock("job-manager: " + project.getId() + "-" + commitId.name());
		transactionManager.mustRunAfterTransaction(() -> lock.unlock());

		JobAuthorizationContext.push(new JobAuthorizationContext(
				project, commitId, SecurityUtils.getUser(), request));
		try {
			// Lock to guarantee uniqueness of build (by project, commit, job and parameters)
			lock.lockInterruptibly();

			BuildSpec buildSpec = project.getBuildSpec(commitId);
			if (buildSpec == null) {
				throw new ExplicitException(String.format(
						"Build spec not defined (project: %s, commit: %s)",
						project.getPath(), commitId.name()));
			}

			validateBuildSpec(project, commitId, buildSpec);

			if (!buildSpec.getJobMap().containsKey(jobName)) {
				throw new ExplicitException(String.format(
						"Job not found (project: %s, commit: %s, job: %s)",
						project.getPath(), commitId.name(), jobName));
			}

			return doSubmit(project, commitId, jobName, paramMap, pipeline, refName, 
					submitter, request, issue, reason);
		} catch (Throwable e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			JobAuthorizationContext.pop();
		}
	}

	private Build doSubmit(Project project, ObjectId commitId, String jobName,
						   Map<String, List<String>> paramMap, String pipeline,
						   String refName, User submitter, @Nullable PullRequest request, 
						   @Nullable Issue issue, String reason) {
		if (request != null) {
			request.setBuildCommitHash(commitId.name());
			dao.persist(request);
		}
		
		Build build = new Build();
		build.setProject(project);
		build.setCommitHash(commitId.name());
		build.setJobName(jobName);
		build.setSubmitDate(new Date());
		build.setStatus(Build.Status.WAITING);
		build.setSubmitReason(reason);
		build.setSubmitter(submitter);
		build.setRefName(refName);
		build.setRequest(request);
		build.setIssue(issue);
		build.setPipeline(pipeline);

		ParamUtils.validateParamMap(build.getJob().getParamSpecs(), paramMap);

		Map<String, List<String>> paramMapToQuery = new HashMap<>(paramMap);
		for (ParamSpec paramSpec : build.getJob().getParamSpecs()) {
			if (paramSpec instanceof SecretParam)
				paramMapToQuery.remove(paramSpec.getName());
		}

		Collection<Build> builds = buildManager.query(project, commitId, jobName,
				refName, Optional.ofNullable(request), Optional.ofNullable(issue), 
				paramMapToQuery, pipeline);

		if (builds.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
				ParamSpec paramSpec = Preconditions.checkNotNull(build.getJob().getParamSpecMap().get(entry.getKey()));
				if (!entry.getValue().isEmpty()) {
					for (String string : entry.getValue()) {
						BuildParam param = new BuildParam();
						param.setBuild(build);
						param.setName(entry.getKey());
						param.setType(paramSpec.getType());
						param.setValue(string);
						build.getParams().add(param);
					}
				} else {
					BuildParam param = new BuildParam();
					param.setBuild(build);
					param.setName(entry.getKey());
					param.setType(paramSpec.getType());
					build.getParams().add(param);
				}
			}

			VariableInterpolator interpolator = new VariableInterpolator(build, build.getParamCombination());
			for (JobDependency dependency : build.getJob().getJobDependencies()) {
				JobDependency interpolated = interpolator.interpolateProperties(dependency);
				new MatrixRunner<>(ParamUtils.getParamMatrix(build, build.getParamCombination(),
						interpolated.getJobParams())) {

					@Override
					public void run(Map<String, List<String>> paramMap) {
						Build dependencyBuild = doSubmit(project, commitId,
								interpolated.getJobName(), paramMap, pipeline,
								refName, submitter, request, issue, reason);
						BuildDependence dependence = new BuildDependence();
						dependence.setDependency(dependencyBuild);
						dependence.setDependent(build);
						dependence.setRequireSuccessful(interpolated.isRequireSuccessful());
						dependence.setArtifacts(interpolated.getArtifacts());
						dependence.setDestinationPath(interpolated.getDestinationPath());
						build.getDependencies().add(dependence);
					}

				}.run();
			}

			for (ProjectDependency dependency : build.getJob().getProjectDependencies()) {
				dependency = interpolator.interpolateProperties(dependency);
				Project dependencyProject = projectManager.findByPath(dependency.getProjectPath());
				if (dependencyProject == null)
					throw new ExplicitException("Unable to find dependency project: " + dependency.getProjectPath());

				Subject subject;
				if (dependency.getAccessTokenSecret() != null) {
					String accessToken = build.getJobAuthorizationContext().getSecretValue(dependency.getAccessTokenSecret());
					User user = userManager.findByAccessToken(accessToken);
					if (user == null) {
						throw new ExplicitException("Unable to access dependency project '"
								+ dependency.getProjectPath() + "': invalid access token");
					}
					subject = user.asSubject();
				} else {
					subject = SecurityUtils.asSubject(0L);
				}

				Build dependencyBuild = dependency.getBuildProvider().getBuild(dependencyProject);
				if (dependencyBuild == null) {
					String errorMessage = String.format("Unable to find dependency build in project '"
							+ dependencyProject.getPath() + "'");
					throw new ExplicitException(errorMessage);
				}

				JobPermission jobPermission = new JobPermission(dependencyBuild.getJobName(), new AccessBuild());
				if (!dependencyProject.isPermittedByLoginUser(jobPermission)
						&& !subject.isPermitted(new ProjectPermission(dependencyProject, jobPermission))) {
					throw new ExplicitException("Unable to access dependency build '"
							+ dependencyBuild.getFQN() + "': permission denied");
				}

				BuildDependence dependence = new BuildDependence();
				dependence.setDependency(dependencyBuild);
				dependence.setDependent(build);
				dependence.setArtifacts(dependency.getArtifacts());
				dependence.setDestinationPath(dependency.getDestinationPath());
				build.getDependencies().add(dependence);
			}

			buildManager.create(build);
			buildSubmitted(build);

			Long buildId = build.getId();
			Long projectId = project.getId();
			Long requestId = PullRequest.idOf(request);
			Long issueId = Issue.idOf(issue);
			sessionManager.runAsyncAfterCommit(() -> {
				SecurityUtils.bindAsSystem();
				Project innerProject = projectManager.load(projectId);
				PullRequest innerRequest;
				if (requestId != null)
					innerRequest = pullRequestManager.load(requestId);
				else
					innerRequest = null;
				Issue innerIssue;
				if (issueId != null)
					innerIssue = issueManager.load(issueId);
				else 
					innerIssue = null;
				for (Build unfinished : buildManager.queryUnfinished(innerProject, jobName, refName,
						Optional.ofNullable(innerRequest), Optional.ofNullable(innerIssue), paramMapToQuery)) {
					if (unfinished.getId() < buildId
							&& (innerRequest != null || gitService.isMergedInto(innerProject, null, unfinished.getCommitId(), commitId))) {
						cancel(unfinished);
					}
				}
			});

			return build;
		} else {
			return builds.iterator().next();
		}
	}

	private void buildSubmitted(Build build) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		projectManager.runOnActiveServer(projectId, () -> {
			FileUtils.cleanDir(buildManager.getStorageDir(projectId, buildNumber));
			return null;
		});
		listenerRegistry.post(new BuildSubmitted(build));
	}

	private boolean isApplicable(JobExecutor executor, Build build) {
		if (executor.getJobRequirement() != null) {
			JobMatch jobMatch = JobMatch.parse(executor.getJobRequirement(), true, true);
			PullRequest request = build.getRequest();
			if (request != null && request.getSource() != null) {
				JobMatchContext sourceContext = new JobMatchContext(
						request.getSourceProject(), request.getSourceBranch(), 
						null, request.getSubmitter(), build.getJobName());
				JobMatchContext targetContext = new JobMatchContext(
						request.getTargetProject(), request.getTargetBranch(), 
						null, request.getSubmitter(), build.getJobName());
				return jobMatch.matches(sourceContext) && jobMatch.matches(targetContext);
			} else {
				return jobMatch.matches(new JobMatchContext(
						build.getProject(), null, build.getCommitId(),
						build.getSubmitter(), build.getJobName()));
			}
		} else {
			return true;
		}
	}

	private JobExecutor getJobExecutor(Build build, @Nullable String jobExecutorName, TaskLogger jobLogger) {
		if (StringUtils.isNotBlank(jobExecutorName)) {
			JobExecutor jobExecutor = null;
			for (JobExecutor each : settingManager.getJobExecutors()) {
				if (each.getName().equals(jobExecutorName)) {
					jobExecutor = each;
					break;
				}
			}
			if (jobExecutor != null) {
				if (!jobExecutor.isEnabled())
					throw new ExplicitException("Specified job executor '" + jobExecutorName + "' is disabled");
				else if (!isApplicable(jobExecutor, build))
					throw new ExplicitException("Specified job executor '" + jobExecutorName + "' is not authorized for current job");
				else
					return jobExecutor;
			} else {
				throw new ExplicitException("Unable to find specified job executor '" + jobExecutorName + "'");
			}
		} else {
			if (!settingManager.getJobExecutors().isEmpty()) {
				for (JobExecutor executor : settingManager.getJobExecutors()) {
					if (executor.isEnabled() && isApplicable(executor, build))
						return executor;
				}
				throw new ExplicitException("No applicable job executor");
			} else {
				jobLogger.log("No job executor defined, auto-discovering...");
				List<JobExecutorDiscoverer> discoverers = new ArrayList<>(OneDev.getExtensions(JobExecutorDiscoverer.class));
				discoverers.sort(Comparator.comparing(JobExecutorDiscoverer::getOrder));
				for (JobExecutorDiscoverer discoverer : discoverers) {
					JobExecutor jobExecutor = discoverer.discover();
					if (jobExecutor != null) {
						jobExecutor.setName("auto-discovered");
						jobLogger.log("Discovered job executor type: "
								+ EditableUtils.getDisplayName(jobExecutor.getClass()));
						return jobExecutor;
					}
				}
				throw new ExplicitException("No job executor discovered");
			}
		}
	}

	private JobExecution execute(Build build) {
		String jobToken = UUID.randomUUID().toString();
		VariableInterpolator interpolator = new VariableInterpolator(build, build.getParamCombination());

		Collection<String> jobSecretsToMask = Sets.newHashSet(jobToken, clusterManager.getCredential());
		TaskLogger jobLogger = logManager.newLogger(build, jobSecretsToMask);
		String jobExecutorName = interpolator.interpolate(build.getJob().getJobExecutor());

		JobExecutor jobExecutor = getJobExecutor(build, jobExecutorName, jobLogger);
		Long projectId = build.getProject().getId();
		String projectPath = build.getProject().getPath();
		String projectGitDir = projectManager.getGitDir(build.getProject().getId()).getAbsolutePath();
		Long buildId = build.getId();
		Long buildNumber = build.getNumber();
		String refName = build.getRefName();
		ObjectId commitId = ObjectId.fromString(build.getCommitHash());
		BuildSpec buildSpec = build.getSpec();

		AtomicInteger maxRetries = new AtomicInteger(0);
		AtomicInteger retryDelay = new AtomicInteger(0);
		List<CacheSpec> caches = new ArrayList<>();
		List<Service> services = new ArrayList<>();
		List<Action> actions = new ArrayList<>();
		long timeout;

		Job job;
		JobAuthorizationContext.push(build.getJobAuthorizationContext());
		Build.push(build);
		try {
			job = build.getJob();

			for (Step step : job.getSteps()) {
				step = interpolator.interpolateProperties(step);
				actions.add(step.getAction(build, jobExecutor, jobToken, build.getParamCombination()));
			}

			for (CacheSpec cache : job.getCaches())
				caches.add(interpolator.interpolateProperties(cache));

			for (String serviceName : job.getRequiredServices()) {
				Service service = buildSpec.getServiceMap().get(serviceName);
				services.add(interpolator.interpolateProperties(service));
			}

			maxRetries.set(job.getMaxRetries());
			retryDelay.set(job.getRetryDelay());
			
			timeout = job.getTimeout();
		} finally {
			Build.pop();
			JobAuthorizationContext.pop();
		}

		AtomicReference<JobExecution> executionRef = new AtomicReference<>(null);
		executionRef.set(new JobExecution(executorService.submit(() -> {
			AtomicInteger retried = new AtomicInteger(0);
			while (true) {
				JobContext jobContext = new JobContext(jobToken, jobExecutor, projectId, projectPath,
						projectGitDir, buildId, buildNumber, actions, refName, commitId, caches, 
						services, timeout, retried.get());
				// Store original job actions as the copy in job context will be fetched from cluster and 
				// some transient fields (such as step object in ServerSideFacade) will not be preserved 
				jobActions.put(jobToken, actions);
				logManager.addJobLogger(jobToken, jobLogger);
				serverStepThreads.put(jobToken, new ArrayList<>());
				try {
					jobExecutor.execute(jobContext, jobLogger);
					break;
				} catch (Throwable e) {
					if (retried.getAndIncrement() < maxRetries.get() && sessionManager.call(new Callable<Boolean>() {

						@Override
						public Boolean call() {
							RetryCondition retryCondition = RetryCondition.parse(job, job.getRetryCondition());

							AtomicReference<String> errorMessage = new AtomicReference<>(null);
							log(e, new TaskLogger() {

								@Override
								public void log(String message, String sessionId) {
									errorMessage.set(message);
								}

							});
							return retryCondition.matches(new RetryContext(buildManager.load(buildId), errorMessage.get()));
						}

					})) {
						log(e, jobLogger);
						jobLogger.warning("Job will be retried after a while...");
						transactionManager.run(() -> {
							Build innerBuild = buildManager.load(buildId);
							innerBuild.setRunningDate(null);
							innerBuild.setPendingDate(null);
							innerBuild.setRetryDate(new Date());
							innerBuild.setStatus(Status.WAITING);
							listenerRegistry.post(new BuildRetrying(innerBuild));
							buildManager.update(innerBuild);
						});
						try {
							Thread.sleep(retryDelay.get() * (long) (Math.pow(2, retried.get())) * 1000L);
						} catch (InterruptedException e2) {
							throw new RuntimeException(e2);
						}
						transactionManager.run(() -> {
							JobExecution execution = executionRef.get();
							if (execution != null)
								execution.updateBeginTime();
							Build innerBuild = buildManager.load(buildId);
							innerBuild.setPendingDate(new Date());
							innerBuild.setStatus(Status.PENDING);
							listenerRegistry.post(new BuildPending(innerBuild));
							buildManager.update(innerBuild);
						});
					} else {
						throw ExceptionUtils.unchecked(e);
					}
				} finally {
					Collection<Thread> threads = serverStepThreads.remove(jobToken);
					synchronized (threads) {
						for (Thread thread : threads)
							thread.interrupt();
					}
					logManager.removeJobLogger(jobToken);
					jobActions.remove(jobToken);
				}
			}
		}), job.getTimeout() * 1000L));

		return executionRef.get();
	}

	private void log(Throwable e, TaskLogger logger) {
		if (e instanceof ExplicitException)
			logger.error(e.getMessage());
		else
			logger.error("Exception catched", e);
	}

	@Override
	public JobContext getJobContext(String jobToken, boolean mustExist) {
		var jobServer = jobServers.get(jobToken);
		if (mustExist && jobServer == null)
			throw new ExplicitException("No job context found for specified job token");
		var jobContext = clusterManager.runOnServer(jobServer, () -> jobContexts.get(jobToken));
		if (mustExist && jobContext == null)
			throw new ExplicitException("No job context found for specified job token");
		return jobContext;
	}

	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.FAILED);
		logManager.newLogger(build).error(errorMessage);
		build.setFinishDate(new Date());
		buildManager.update(build);
		listenerRegistry.post(new BuildFinished(build));
	}

	@Listen
	public void on(ProjectCreated event) {
		schedule(event.getProject(), false);
	}

	@Listen
	public void on(ProjectDeleted event) {
		unschedule(event.getProjectId());
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware && ((CommitAware) event).getCommit() != null) {
			ObjectId commitId = ((CommitAware) event).getCommit().getCommitId();
			if (!commitId.equals(ObjectId.zeroId())) {
				String pipeline;
				if (event instanceof BuildEvent)
					pipeline = ((BuildEvent) event).getBuild().getPipeline();
				else
					pipeline = UUID.randomUUID().toString();
				PullRequest request = null;
				if (event instanceof PullRequestEvent)
					request = ((PullRequestEvent) event).getRequest();
				JobAuthorizationContext jobAuthorizationContext = new JobAuthorizationContext(
						event.getProject(), commitId, SecurityUtils.getUser(), request);
				JobAuthorizationContext.push(jobAuthorizationContext);
				try {
					BuildSpec buildSpec = event.getProject().getBuildSpec(commitId);
					if (buildSpec != null) {
						validateBuildSpec(event.getProject(), commitId, buildSpec);
						for (Job job : buildSpec.getJobMap().values()) {
							TriggerMatch match = job.getTriggerMatch(event);
							if (match != null) {
								Map<String, List<List<String>>> paramMatrix =
										ParamUtils.getParamMatrix(null, null, match.getParams());
								Long projectId = event.getProject().getId();

								// run asynchrously as session may get closed due to exception
								sessionManager.runAsyncAfterCommit(new Runnable() {

									@Override
									public void run() {
										SecurityUtils.bindAsSystem();
										Project project = projectManager.load(projectId);
										try {
											new MatrixRunner<>(paramMatrix) {

												@Override
												public void run(Map<String, List<String>> paramMap) {
													submit(project, commitId, job.getName(), paramMap,
															pipeline, match.getRefName(), SecurityUtils.getUser(),
															match.getRequest(), match.getIssue(), match.getReason());
												}

											}.run();
										} catch (Throwable e) {
											String message = String.format("Error submitting build (project: %s, commit: %s, job: %s)",
													project.getPath(), commitId.name(), job.getName());
											logger.error(message, e);
										}
									}

								});
							}
						}
					}
				} catch (Throwable e) {
					String message = String.format("Error checking job triggers (project: %s, commit: %s)",
							event.getProject().getPath(), commitId.name());
					logger.error(message, e);
				} finally {
					JobAuthorizationContext.pop();
				}
			}
		}
	}

	@Transactional
	@Override
	public void resubmit(Build build, String reason) {
		if (build.isFinished()) {
			JobAuthorizationContext.push(build.getJobAuthorizationContext());
			try {
				BuildSpec buildSpec = build.getSpec();

				if (buildSpec == null) {
					throw new ExplicitException(String.format(
							"Build spec not defined (project: %s, commit: %s)",
							build.getProject().getPath(), build.getCommitHash()));
				}

				validateBuildSpec(build.getProject(), build.getCommitId(), buildSpec);

				if (!buildSpec.getJobMap().containsKey(build.getJobName())) {
					throw new ExplicitException(String.format(
							"Job not found (project: %s, commit: %s, job: %s)",
							build.getProject().getPath(), build.getCommitId().name(), build.getJobName()));
				}

				build.setStatus(Build.Status.WAITING);
				build.setFinishDate(null);
				build.setPendingDate(null);
				build.setRetryDate(null);
				build.setRunningDate(null);
				build.setSubmitDate(new Date());
				build.setSubmitter(SecurityUtils.getUser());
				build.setSubmitReason(reason);
				build.setCanceller(null);
				build.setAgent(null);
				build.getCheckoutPaths().clear();

				buildParamManager.deleteParams(build);
				for (Map.Entry<String, List<String>> entry : build.getParamMap().entrySet()) {
					ParamSpec paramSpec = build.getJob().getParamSpecMap().get(entry.getKey());
					Preconditions.checkNotNull(paramSpec);
					String type = paramSpec.getType();
					List<String> values = entry.getValue();
					if (!values.isEmpty()) {
						for (String value : values) {
							BuildParam param = new BuildParam();
							param.setBuild(build);
							param.setName(entry.getKey());
							param.setType(type);
							param.setValue(value);
							build.getParams().add(param);
							buildParamManager.create(param);
						}
					} else {
						BuildParam param = new BuildParam();
						param.setBuild(build);
						param.setName(paramSpec.getName());
						param.setType(type);
						build.getParams().add(param);
						buildParamManager.create(param);
					}
				}
				buildManager.update(build);
				buildSubmitted(build);
			} finally {
				JobAuthorizationContext.pop();
			}

			for (BuildDependence dependence : build.getDependencies()) {
				Build dependency = dependence.getDependency();
				if (dependence.isRequireSuccessful() && !dependency.isSuccessful())
					resubmit(dependency, "Resubmitted by dependent build");
			}
		} else {
			throw new ExplicitException("Build #" + build.getNumber() + " not finished yet");
		}
	}

	@Transactional
	@Override
	public void resume(Build build) {
		Long buildId = build.getId();
		JobContext jobContext = getJobContext(buildId);
		if (jobContext != null) {
			String jobServer = jobServers.get(jobContext.getJobToken());
			if (jobServer != null) {
				clusterManager.runOnServer(jobServer, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() {
						JobContext jobContext = getJobContext(buildId);
						if (jobContext != null) {
							JobRunnable jobRunnable = jobRunnables.get(jobContext.getJobToken());
							if (jobRunnable != null)
								jobRunnable.resume(jobContext);
						}
						return null;
					}

				});
				build.setPaused(false);
				listenerRegistry.post(new BuildUpdated(build));
			}
		}
	}

	@Override
	public WebShell openShell(Long buildId, Terminal terminal) {
		JobContext jobContext = getJobContext(buildId);
		if (jobContext != null) {
			String jobToken = jobContext.getJobToken();
			String shellServer = jobServers.get(jobToken);
			if (shellServer != null) {
				if (SecurityUtils.isAdministrator() || jobContext.getJobExecutor().isShellAccessEnabled()) {
					clusterManager.runOnServer(shellServer, () -> {
						JobContext innerJobContext = getJobContext(jobToken, true);
						JobRunnable jobRunnable = jobRunnables.get(innerJobContext.getJobToken());
						if (jobRunnable != null) {
							Shell shell = jobRunnable.openShell(innerJobContext, terminal);
							jobShells.put(terminal.getSessionId(), shell);
						} else {
							throw new ExplicitException("Job shell not ready");
						}
						return null;
					});

					return new WebShell(buildId, terminal.getSessionId()) {

						private static final long serialVersionUID = 1L;

						@Override
						public void sendInput(String input) {
							clusterManager.submitToServer(shellServer, () -> {
								try {
									Shell shell = jobShells.get(terminal.getSessionId());
									if (shell != null)
										shell.sendInput(input);
								} catch (Exception e) {
									logger.error("Error sending shell input", e);
								}
								return null;
							});
						}

						@Override
						public void resize(int rows, int cols) {
							clusterManager.submitToServer(shellServer, () -> {
								try {
									Shell shell = jobShells.get(terminal.getSessionId());
									if (shell != null)
										shell.resize(rows, cols);
								} catch (Exception e) {
									logger.error("Error resizing shell", e);
								}
								return null;
							});
						}

						@Override
						public void exit() {
							clusterManager.submitToServer(shellServer, () -> {
								try {
									Shell shell = jobShells.remove(terminal.getSessionId());
									if (shell != null)
										shell.exit();
								} catch (Exception e) {
									logger.error("Error exiting shell", e);
								}
								return null;
							});
						}

					};
				} else {
					throw new UnauthorizedException();
				}
			} else {
				throw new ExplicitException("Job shell not ready");
			}
		} else {
			throw new ExplicitException("Job shell not ready");
		}
	}

	@Override
	public Shell getShell(String sessionId) {
		return jobShells.get(sessionId);
	}

	@Override
	public JobContext getJobContext(Long buildId) {
		Map<String, JobContext> result = clusterManager.runOnAllServers(() -> {
			for (Map.Entry<String, JobContext> entry : jobContexts.entrySet()) {
				JobContext jobContext = entry.getValue();
				if (jobContext.getBuildId().equals(buildId))
					return jobContext;
			}
			return null;
		});
		return result.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
	}

	@Transactional
	@Override
	public void cancel(Build build) {
		Long projectId = build.getProject().getId();
		Long buildId = build.getId();
		Long userId = User.idOf(SecurityUtils.getUser());
		projectManager.runOnActiveServer(projectId, () -> {
			JobExecution execution = jobExecutions.get(buildId);
			if (execution != null) {
				execution.cancel(userId);
			} else {
				transactionManager.run(() -> {
					Build innerBuild = buildManager.load(buildId);
					if (!innerBuild.isFinished()) {
						innerBuild.setStatus(Status.CANCELLED);
						innerBuild.setFinishDate(new Date());
						innerBuild.setCanceller(userManager.load(userId));
						buildManager.update(innerBuild);
						listenerRegistry.post(new BuildFinished(innerBuild));
					}
				});
			}
			return null;
		});
	}

	@Listen
	public void on(SystemStarting event) {
		var hazelcastInstance = clusterManager.getHazelcastInstance();
		allocatedCaches = hazelcastInstance.getMap("allocatedCaches");
		jobServers = hazelcastInstance.getMap("jobServers");
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		var localServer = clusterManager.getLocalServerAddress();
		for (var projectId: projectManager.getIds()) {
			if (localServer.equals(projectManager.getActiveServer(projectId, false))) 
				schedule(projectManager.load(projectId), false);
		}
		thread = new Thread(this);
		thread.start();
		maintenanceTaskId = taskScheduler.schedule(this);
	}
	
	@Sessional
	@Listen
	public void on(ActiveServerChanged event) {
		for (var projectId: event.getProjectIds())
			schedule(projectManager.load(projectId), false);
	}

	@Sessional
	@Listen
	public void on(DefaultBranchChanged event) {
		schedule(event.getProject(), false);
	}

	@Sessional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		Project project = event.getProject();
		if (branch != null && branch.equals(project.getDefaultBranch()) && !event.getNewCommitId().equals(ObjectId.zeroId()))
			schedule(project, false);
	}

	@Sessional
	@Override
	public void schedule(Project project, boolean recursive) {
		Collection<String> tasksOfProject = new HashSet<>();
		try {
			String defaultBranch = project.getDefaultBranch();
			if (defaultBranch != null) {
				ObjectId commitId = project.getObjectId(defaultBranch, false);
				if (commitId != null) {
					JobAuthorizationContext.push(new JobAuthorizationContext(project, commitId, SecurityUtils.getUser(), null));
					try {
						BuildSpec buildSpec = project.getBuildSpec(commitId);
						if (buildSpec != null) {
							validateBuildSpec(project, commitId, buildSpec);
							ScheduledTimeReaches event = new ScheduledTimeReaches(project);
							for (Job job : buildSpec.getJobMap().values()) {
								for (JobTrigger trigger : job.getTriggers()) {
									if (trigger instanceof ScheduleTrigger) {
										ScheduleTrigger scheduledTrigger = (ScheduleTrigger) trigger;
										TriggerMatch match = trigger.matches(event, job);
										if (match != null) {
											String taskId = taskScheduler.schedule(newSchedulableTask(project, commitId, job, scheduledTrigger, match.getRefName(), match.getReason()));
											tasksOfProject.add(taskId);
										}
									}
								}
							}
						}
					} catch (BuildSpecParseException e) {
						logger.warn("Malformed build spec (project: {}, branch: {})",
								project.getPath(), defaultBranch);
					} finally {
						JobAuthorizationContext.pop();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error scheduling project '" + project.getPath() + "'", e);
		} finally {
			tasksOfProject = projectTasks.put(project.getId(), tasksOfProject);
			if (tasksOfProject != null)
				tasksOfProject.forEach(taskScheduler::unschedule);
		}
		if (recursive) {
			for (var child : project.getChildren())
				schedule(child, true);
		}
	}

	@Sessional
	@Override
	public void unschedule(Long projectId) {
		var tasksOfProject = projectTasks.remove(projectId);
		if (tasksOfProject != null)
			tasksOfProject.forEach(taskScheduler::unschedule);
	}

	private SchedulableTask newSchedulableTask(Project project, ObjectId commitId, Job job,
											   ScheduleTrigger trigger, String refName, 
											   String reason) {
		Long projectId = project.getId();
		var localServer = clusterManager.getLocalServerAddress();
		return new SchedulableTask() {
			
			@Override
			public void execute() {
				if (thread != null && localServer.equals(projectManager.getActiveServer(projectId, false))) {
					sessionManager.run(() -> {
						SecurityUtils.bindAsSystem();
						Project aProject = projectManager.load(projectId);
						String pipeline = UUID.randomUUID().toString();
						new MatrixRunner<>(ParamUtils.getParamMatrix(null, null, trigger.getParams())) {

							@Override
							public void run(Map<String, List<String>> paramMap) {
								submit(aProject, commitId, job.getName(), paramMap, pipeline, 
										refName, SecurityUtils.getUser(), null, null, reason);
							}

						}.run();
					});
				}
			}

			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return CronScheduleBuilder.cronSchedule(trigger.getCronExpression());
			}

		};
	}

	@Listen
	public void on(SystemStopping event) {
		Thread copy = thread;
		thread = null;
		if (copy != null) {
			try {
				copy.join();
			} catch (InterruptedException ignored) {
			}
		}
		projectTasks.values().forEach(it -> it.forEach(taskScheduler::unschedule));
		projectTasks.clear();
		
		if (maintenanceTaskId != null)
			taskScheduler.unschedule(maintenanceTaskId);
	}

	@Override
	public void run() {
		while (!jobExecutions.isEmpty() || thread != null) {
			if (thread == null) {
				if (!jobExecutions.isEmpty())
					logger.info("Waiting for jobs to finish...");
				for (var execution: jobExecutions.values()) {
					if (!execution.isDone() && !execution.isCancelled())
						execution.cancel(User.SYSTEM_ID);
				}
			}
			try {
				if (clusterManager.isLeaderServer()) {
					Map<String, Collection<Long>> buildIds = new HashMap<>();
					for (var entry : buildManager.queryUnfinished().entrySet()) {
						var buildId = entry.getKey();
						var projectId = entry.getValue();
						String activeServer = projectManager.getActiveServer(projectId, false);
						if (activeServer != null && clusterManager.getOnlineServers().contains(activeServer)) {
							var buildIdsOfServer = buildIds.computeIfAbsent(activeServer, k -> new ArrayList<>());
							buildIdsOfServer.add(buildId);
						}
					}

					Collection<Future<?>> futures = new ArrayList<>();
					for (var entry : buildIds.entrySet()) {
						var server = entry.getKey();
						var buildIdsOfServer = entry.getValue();
						futures.add(clusterManager.submitToServer(server, () -> {
							transactionManager.run(() -> {
								for (Long buildId : buildIdsOfServer) {
									Build build = buildManager.load(buildId);
									if (build.getStatus() == Status.PENDING) {
										JobExecution execution = jobExecutions.get(build.getId());
										if (execution != null) {
											execution.updateBeginTime();
										} else if (thread != null) {
											try {
												jobExecutions.put(build.getId(), execute(build));
											} catch (Throwable t) {
												ExplicitException explicitException = ExceptionUtils.find(t, ExplicitException.class);
												if (explicitException != null)
													markBuildError(build, explicitException.getMessage());
												else
													markBuildError(build, Throwables.getStackTraceAsString(t));
											}
										}
									} else if (build.getStatus() == Status.RUNNING) {
										JobExecution execution = jobExecutions.get(build.getId());
										if (execution != null) {
											if (execution.isTimedout())
												execution.cancel(null);
										} else {
											build.setStatus(Status.PENDING);
										}
									} else if (build.getStatus() == Status.WAITING) {
										if (build.getRetryDate() != null) {
											JobExecution execution = jobExecutions.get(build.getId());
											if (execution == null && thread != null) {
												build.setStatus(Status.PENDING);
												build.setPendingDate(new Date());
												listenerRegistry.post(new BuildPending(build));
											}
										} else if (build.getDependencies().stream().anyMatch(it -> it.isRequireSuccessful()
												&& it.getDependency().isFinished()
												&& it.getDependency().getStatus() != Status.SUCCESSFUL)) {
											markBuildError(build, "Some dependencies are required to be successful but failed");
										} else if (build.getDependencies().stream().allMatch(it -> it.getDependency().isFinished())) {
											build.setStatus(Status.PENDING);
											build.setPendingDate(new Date());
											listenerRegistry.post(new BuildPending(build));
										}
									}
								}
							});
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

				sessionManager.run(() -> {
					for (Iterator<Map.Entry<Long, JobExecution>> it = jobExecutions.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<Long, JobExecution> entry = it.next();
						Build build = buildManager.get(entry.getKey());
						JobExecution execution = entry.getValue();
						if (build == null || build.isFinished()) {
							it.remove();
							execution.cancel(null);
						} else if (execution.isDone()) {
							it.remove();
							TaskLogger jobLogger = logManager.newLogger(build);
							try {
								execution.check();
								build.setStatus(Status.SUCCESSFUL);
								jobLogger.log("Job finished");
							} catch (TimeoutException e) {
								build.setStatus(Status.TIMED_OUT);
							} catch (java.util.concurrent.CancellationException e) {
								if (e instanceof CancellationException) {
									Long cancellerId = ((CancellationException) e).getCancellerId();
									if (cancellerId != null)
										build.setCanceller(userManager.load(cancellerId));
								}
								build.setStatus(Status.CANCELLED);
							} catch (ExecutionException e) {
								build.setStatus(Status.FAILED);
								ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
								if (explicitException != null)
									jobLogger.error(explicitException.getMessage());
								else if (ExceptionUtils.find(e, FailedException.class) == null)
									jobLogger.error("Error running job", e);
							} catch (InterruptedException ignored) {
							} finally {
								build.setFinishDate(new Date());
								buildManager.update(build);
								listenerRegistry.post(new BuildFinished(build));
							}
						}
					}
				});
				Thread.sleep(CHECK_INTERVAL);
			} catch (Throwable e) {
				logger.error("Error checking unfinished builds", e);
			}
		}
	}

	@Transactional
	@Listen
	public void on(BuildFinished event) {
		Build build = event.getBuild();
		JobAuthorizationContext.push(build.getJobAuthorizationContext());
		Build.push(build);
		try {
			VariableInterpolator interpolator = new VariableInterpolator(build, build.getParamCombination());
			Map<String, String> placeholderValues = new HashMap<>();
			placeholderValues.put(BUILD_VERSION, build.getVersion());
			if (build.getJob() != null) {
				for (PostBuildAction action : build.getJob().getPostBuildActions()) {
					action = interpolator.interpolateProperties(action);
					if (ActionCondition.parse(build.getJob(), action.getCondition()).matches(build))
						action.execute(build);
				}
			} else {
				throw new ExplicitException("Job not found");
			}
		} catch (Throwable e) {
			String message = String.format("Error processing post build actions (project: %s, commit: %s, job: %s)",
					build.getProject().getPath(), build.getCommitHash(), build.getJobName());
			logger.error(message, e);
		} finally {
			Build.pop();
			JobAuthorizationContext.pop();
		}
	}

	@Override
	public boolean canPullCode(HttpServletRequest request, Project project) {
		String jobToken = SecurityUtils.getBearerToken(request);
		if (jobToken != null) {
			JobContext jobContext = getJobContext(jobToken, false);
			if (jobContext != null)
				return jobContext.getProjectId().equals(project.getId());
		}
		return false;
	}

	@Override
	public Map<CacheInstance, String> allocateCaches(JobContext jobContext, CacheAllocationRequest request) {
		String jobToken = jobContext.getJobToken();
		return clusterManager.runOnServer(clusterManager.getLeaderServerAddress(), () -> {
			synchronized (allocatedCaches) {
				JobContext innerJobContext = getJobContext(jobToken, true);

				List<CacheInstance> sortedInstances = new ArrayList<>(request.getInstances().keySet());
				sortedInstances.sort((o1, o2) -> request.getInstances().get(o2).compareTo(request.getInstances().get(o1)));

				Collection<String> allAllocated = new HashSet<>();
				var activeJobTokens = getActiveJobTokens();
				Collection<String> removeKeys = new HashSet<>();
				for (var entry : allocatedCaches.entrySet()) {
					if (activeJobTokens.contains(entry.getKey()))
						allAllocated.addAll(entry.getValue());
					else
						removeKeys.add(entry.getKey());
				}
				for (var key : removeKeys)
					allocatedCaches.remove(key);

				Map<CacheInstance, String> allocations = new HashMap<>();

				Collection<String> allocatedCachesOfJob = new ArrayList<>();
				for (CacheSpec cacheSpec : innerJobContext.getCacheSpecs()) {
					Optional<CacheInstance> result = sortedInstances
							.stream()
							.filter(it -> it.getCacheKey().equals(cacheSpec.getNormalizedKey()))
							.filter(it -> !allAllocated.contains(it.getCacheUUID()))
							.findFirst();
					CacheInstance allocation;
					allocation = result.orElseGet(() -> new CacheInstance(cacheSpec.getNormalizedKey(), UUID.randomUUID().toString()));
					allocations.put(allocation, cacheSpec.getPath());
					allocatedCachesOfJob.add(allocation.getCacheUUID());
					allAllocated.add(allocation.getCacheUUID());
				}

				Consumer<CacheInstance> deletionMarker = instance -> {
					long ellapsed = request.getCurrentTime().getTime() - request.getInstances().get(instance).getTime();
					if (ellapsed > innerJobContext.getJobExecutor().getCacheTTL() * 24L * 3600L * 1000L) {
						allocations.put(instance, null);
						allocatedCachesOfJob.add(instance.getCacheUUID());
						allAllocated.add(instance.getCacheUUID());
					}
				};

				allocatedCaches.put(jobToken, allocatedCachesOfJob);

				request.getInstances().keySet()
						.stream()
						.filter(it -> !allAllocated.contains(it.getCacheUUID()))
						.forEach(deletionMarker);

				return allocations;
			}
		});
	}

	@Override
	public void runJob(String server, ClusterRunnable runnable) {
		Future<?> future = null;
		try {
			future = clusterManager.submitToServer(server, () -> {
				runnable.run();
				return null;
			});

			// future.get() here does not respond to thread interruption
			while (!future.isDone())
				Thread.sleep(1000);
			future.get(); // call get() to throw possible execution exceptions
		} catch (InterruptedException e) {
			if (future != null)
				future.cancel(true);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void runJob(JobContext jobContext, JobRunnable runnable) {
		while (thread == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		String jobToken = jobContext.getJobToken();
		jobServers.put(jobToken, clusterManager.getLocalServerAddress());
		jobContexts.put(jobToken, jobContext);
		jobRunnables.put(jobToken, runnable);
		try {
			TaskLogger jobLogger = logManager.getJobLogger(jobToken);
			if (jobLogger == null) {
				var activeServer = projectManager.getActiveServer(jobContext.getProjectId(), true);
				jobLogger = new ServerJobLogger(activeServer, jobContext.getJobToken());
				logManager.addJobLogger(jobToken, jobLogger);
				try {
					runnable.run(jobLogger);
				} finally {
					logManager.removeJobLogger(jobToken);
				}
			} else {
				runnable.run(jobLogger);
			}
		} finally {
			jobRunnables.remove(jobToken);
			jobContexts.remove(jobToken);
			jobServers.remove(jobToken);
		}
	}

	@Override
	public void reportJobWorkspace(JobContext jobContext, String jobWorkspace) {
		transactionManager.run(() -> {
			Build build = buildManager.load(jobContext.getBuildId());
			
			CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());
			entryFacade.traverse((LeafVisitor<Void>) (executable, position) -> {
				if (executable instanceof CheckoutFacade) {
					CheckoutFacade checkoutFacade = (CheckoutFacade) executable;
					var checkoutPath = jobWorkspace;
					if (checkoutFacade.getCheckoutPath() != null)
						checkoutPath += "/" + checkoutFacade.getCheckoutPath();
					checkoutPath = checkoutPath.replace('\\', '/');
					checkoutPath = Paths.get(checkoutPath).normalize().toString();
					checkoutPath = checkoutPath.replace('\\', '/');
					build.getCheckoutPaths().add(checkoutPath);
				}
				return null;
			}, new ArrayList<>());
			
			buildManager.update(build);
		});
	}
	
	@Sessional
	@Override
	public void copyDependencies(JobContext jobContext, File tempDir) {
		Build build = buildManager.load(jobContext.getBuildId());
		for (BuildDependence dependence : build.getDependencies()) {
			if (dependence.getArtifacts() != null) {
				Build dependency = dependence.getDependency();

				File targetDir;
				if (dependence.getDestinationPath() != null) {
					targetDir = new File(tempDir, dependence.getDestinationPath());
					FileUtils.createDir(targetDir);
				} else {
					targetDir = tempDir;
				}

				String dependencyActiveServer = projectManager.getActiveServer(
						dependency.getProject().getId(), true);
				if (dependencyActiveServer.equals(clusterManager.getLocalServerAddress())) {
					LockUtils.read(dependency.getArtifactsLockName(), () -> {
						File artifactsDir = dependency.getArtifactsDir();
						if (artifactsDir.exists()) {
							PatternSet patternSet = PatternSet.parse(dependence.getArtifacts());
							patternSet.getExcludes().add(Project.SHARE_TEST_DIR + "/**");
							int baseLen = artifactsDir.getAbsolutePath().length() + 1;
							for (File file : FileUtils.listFiles(artifactsDir, patternSet.getIncludes(), patternSet.getExcludes())) {
								FileUtils.copyFile(file,
										new File(targetDir, file.getAbsolutePath().substring(baseLen)));
							}
						}
						return null;
					});
				} else {
					String serverUrl = clusterManager.getServerUrl(dependencyActiveServer);
					Client client = ClientBuilder.newClient();
					try {
						WebTarget target = client.target(serverUrl).path("~api/cluster/artifacts")
								.queryParam("projectId", dependency.getProject().getId())
								.queryParam("buildNumber", dependency.getNumber())
								.queryParam("artifacts", dependence.getArtifacts());
						Invocation.Builder builder = target.request();
						builder.header(HttpHeaders.AUTHORIZATION, KubernetesHelper.BEARER + " "
								+ clusterManager.getCredential());

						try (Response response = builder.get()) {
							KubernetesHelper.checkStatus(response);
							try (InputStream is = response.readEntity(InputStream.class)) {
								FileUtils.untar(is, targetDir, false);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					} finally {
						client.close();
					}
				}
			}
		}
	}

	@Override
	public Map<String, byte[]> runServerStep(JobContext jobContext, List<Integer> stepPosition,
											 File inputDir, Map<String, String> placeholderValues,
											 boolean callByAgent, TaskLogger logger) {
		String activeServer = projectManager.getActiveServer(jobContext.getProjectId(), true);
		if (activeServer.equals(clusterManager.getLocalServerAddress())) {
			Thread thread = Thread.currentThread();
			Collection<Thread> threads = serverStepThreads.get(jobContext.getJobToken());
			if (callByAgent && threads != null) synchronized (threads) {
				threads.add(thread);
			}
			try {
				List<Action> actions = jobActions.get(jobContext.getJobToken());
				if (actions != null) {
					ServerSideFacade serverSideFacade = (ServerSideFacade) LeafFacade.of(actions, stepPosition);
					var serverSideStep = (ServerSideStep) serverSideFacade.getStep();
					var transformedServerSideStep = new EditableStringTransformer(t -> replacePlaceholders(t, placeholderValues)).transformProperties(serverSideStep, Interpolative.class);

					if (transformedServerSideStep.requireCommitIndex()) {
						logger.log("Waiting for commit to be indexed...");
						codeIndexManager.indexAsync(jobContext.getProjectId(), jobContext.getCommitId());
						while (!codeIndexManager.isIndexed(jobContext.getProjectId(), jobContext.getCommitId())) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
					return sessionManager.call(() -> {
						Build build = buildManager.load(jobContext.getBuildId());
						return transformedServerSideStep.run(build, inputDir, logger);
					});
				} else {
					throw new IllegalStateException("Job actions not found");
				}
			} finally {
				if (callByAgent && threads != null) synchronized (threads) {
					threads.remove(thread);
				}
			}
		} else {
			String serverUrl = clusterManager.getServerUrl(activeServer);
			return KubernetesHelper.runServerStep(sslFactory, serverUrl, jobContext.getJobToken(), 
					stepPosition, inputDir, Lists.newArrayList("**"), Lists.newArrayList(), 
					placeholderValues, logger);
		}
	}
	
	private Collection<String> getActiveJobTokens() {
		var activeJobTokens = new HashSet<String>();
		for (var value: clusterManager.runOnAllServers(() -> jobContexts.keySet()).values()) {
			activeJobTokens.addAll(value);
		}
		return activeJobTokens;
	}

	@Override
	public void execute() {
		if (clusterManager.isLeaderServer()) {
			var activeJobTokens = getActiveJobTokens();
			jobServers.removeAll(it -> !activeJobTokens.contains(it.getKey()));
		}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(4, 0);
	}
	
}