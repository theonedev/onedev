package io.onedev.server.job;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.onedev.commons.utils.ExceptionUtils.find;
import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;
import static io.onedev.server.buildspec.param.ParamUtils.resolveParams;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static org.eclipse.jgit.lib.Constants.R_HEADS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.joda.time.DateTime;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.hazelcast.map.IMap;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CheckoutFacade;
import io.onedev.k8shelper.CompositeFacade;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.LeafVisitor;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.k8shelper.ServerStepResult;
import io.onedev.k8shelper.ServiceFacade;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecParseException;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.action.condition.ActionCondition;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.buildspec.job.retrycondition.RetryCondition;
import io.onedev.server.buildspec.job.retrycondition.RetryContext;
import io.onedev.server.buildspec.job.trigger.ScheduleTrigger;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.buildspec.step.ServerSideStep;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.AccessTokenManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.ProjectDeleted;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.ScheduledTimeReaches;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.event.project.build.BuildPending;
import io.onedev.server.event.project.build.BuildRetrying;
import io.onedev.server.event.project.build.BuildSubmitted;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.HttpResponseAwareException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.ServerJobLogger;
import io.onedev.server.job.match.JobMatch;
import io.onedev.server.job.match.JobMatchContext;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.jobexecutor.DockerAware;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.WebShell;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.concurrent.BatchWorkManager;
import io.onedev.server.util.concurrent.BatchWorker;
import io.onedev.server.util.concurrent.Prioritized;
import io.onedev.server.util.concurrent.WorkExecutor;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.EditableStringTransformer;
import io.onedev.server.web.editable.EditableUtils;
import nl.altindag.ssl.SSLFactory;

@Singleton
public class DefaultJobManager implements JobManager, Runnable, CodePullAuthorizationSource, Serializable {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds

	private static final int CACHE_SCHEDULE_PRIORITY = 10;

	private static final int MAINTENANCE_PRIORITY = 50;
	
	private static final String TIMEOUT_MESSAGE = "Job execution timed out";

	private static final Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);

	private final Map<Long, Future<Boolean>> jobFutures = new ConcurrentHashMap<>();

	private final Map<String, Collection<Thread>> serverStepThreads = new ConcurrentHashMap<>();

	private final Map<String, List<Action>> jobActions = new ConcurrentHashMap<>();

	private final Map<String, JobRunnable> jobRunnables = new ConcurrentHashMap<>();
	
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
	
	private final AccessTokenManager accessTokenManager;

	private final SettingManager settingManager;

	private final ExecutorService executorService;

	private final BuildParamManager buildParamManager;

	private final TaskScheduler taskScheduler;

	private final Validator validator;

	private final ClusterManager clusterManager;
	
	private final GitService gitService;
	
	private final SSLFactory sslFactory;

	private final BatchWorkManager batchWorkManager;

	private final WorkExecutor workExecutor;

	private volatile Thread thread;

	private final Map<String, JobContext> jobContexts = new ConcurrentHashMap<>();

	private volatile IMap<String, String> jobServers;
	
	private volatile IMap<String, Date> sequentialKeys;

	private volatile Map<String, List<JobSchedule>> branchSchedules;
	
	private volatile String maintenanceTaskId;
	
	private volatile String branchSchedulesTaskId;

	@Inject
	public DefaultJobManager(BuildManager buildManager, AccessTokenManager accessTokenManager, UserManager userManager, 
							 ListenerRegistry listenerRegistry, SettingManager settingManager, TransactionManager transactionManager, 
							 LogManager logManager, ExecutorService executorService, SessionManager sessionManager, 
							 BuildParamManager buildParamManager, ProjectManager projectManager, Validator validator, 
							 TaskScheduler taskScheduler, ClusterManager clusterManager, 
							 PullRequestManager pullRequestManager, IssueManager issueManager, GitService gitService, 
							 SSLFactory sslFactory, Dao dao, BatchWorkManager batchWorkManager, WorkExecutor workExecutor) {
		this.dao = dao;
		this.settingManager = settingManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.accessTokenManager = accessTokenManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.logManager = logManager;
		this.executorService = executorService;
		this.sessionManager = sessionManager;
		this.buildParamManager = buildParamManager;
		this.projectManager = projectManager;
		this.validator = validator;
		this.taskScheduler = taskScheduler;
		this.clusterManager = clusterManager;
		this.pullRequestManager = pullRequestManager;
		this.issueManager = issueManager;
		this.gitService = gitService;
		this.sslFactory = sslFactory;
		this.batchWorkManager = batchWorkManager;
		this.workExecutor = workExecutor;
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
						Map<String, List<String>> paramMap, String refName, 
						User submitter, @Nullable PullRequest request,
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
				var errorMessage = String.format(
						"Job not found (project: %s, commit: %s, job: %s)",
						project.getPath(), commitId.name(), jobName);
				throw new HttpResponseAwareException(SC_BAD_REQUEST, errorMessage);
			}

			return doSubmit(project, commitId, jobName, paramMap, refName,
					submitter, request, issue, reason);
		} catch (ValidationException e) {
			throw new HttpResponseAwareException(SC_BAD_REQUEST, e.getMessage());
		} catch (Throwable e) {
			throw ExceptionUtils.unchecked(e);
		} finally {
			JobAuthorizationContext.pop();
		}
	}

	private Build doSubmit(Project project, ObjectId commitId, String jobName,
						   Map<String, List<String>> paramMap, String refName, 
						   User submitter, @Nullable PullRequest request, 
						   @Nullable Issue issue, String reason) {
		if (request != null) {
			request.setBuildCommitHash(commitId.name());
			dao.persist(request);
		}
		
		Build build = new Build();
		build.setProject(project);
		build.setCommitHash(commitId.name());
		build.setJobName(jobName);
		build.setJobToken(UUID.randomUUID().toString());
		build.setSubmitDate(new Date());
		build.setStatus(Build.Status.WAITING);
		build.setSubmitReason(reason);
		build.setSubmitter(submitter);
		build.setRefName(refName);
		build.setRequest(request);
		build.setIssue(issue);

		Project.push(project);
		try {
			ParamUtils.validateParamMap(build.getJob().getParamSpecs(), paramMap);
		} finally {
			Project.pop();
		}

		Map<String, List<String>> paramMapToQuery = new HashMap<>(paramMap);
		for (ParamSpec paramSpec : build.getJob().getParamSpecs()) {
			if (paramSpec instanceof SecretParam)
				paramMapToQuery.remove(paramSpec.getName());
		}

		Collection<Build> builds = buildManager.query(project, commitId, jobName,
				refName, Optional.ofNullable(request), Optional.ofNullable(issue), 
				paramMapToQuery);

		if (builds.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
				ParamSpec paramSpec = checkNotNull(build.getJob().getParamSpecMap().get(entry.getKey()));
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
				var dependencyParamMaps = resolveParams(build, build.getParamCombination(), 
						interpolated.getParamMatrix(), interpolated.getExcludeParamMaps());
				for (var dependencyParamMap: dependencyParamMaps) {
					Build dependencyBuild = doSubmit(project, commitId,
							interpolated.getJobName(), dependencyParamMap, 
							refName, submitter, request, issue, reason);
					BuildDependence dependence = new BuildDependence();
					dependence.setDependency(dependencyBuild);
					dependence.setDependent(build);
					dependence.setRequireSuccessful(interpolated.isRequireSuccessful());
					dependence.setArtifacts(interpolated.getArtifacts());
					dependence.setDestinationPath(interpolated.getDestinationPath());
					build.getDependencies().add(dependence);
				}
			}

			for (ProjectDependency dependency : build.getJob().getProjectDependencies()) {
				dependency = interpolator.interpolateProperties(dependency);
				Project dependencyProject = projectManager.findByPath(dependency.getProjectPath());
				if (dependencyProject == null)
					throw new ExplicitException("Unable to find dependency project: " + dependency.getProjectPath());

				Subject subject;
				if (dependency.getAccessTokenSecret() != null) {
					String secretValue = build.getJobAuthorizationContext().getSecretValue(dependency.getAccessTokenSecret());
					var accessToken = accessTokenManager.findByValue(secretValue);
					if (accessToken == null) {
						throw new ExplicitException("Unable to access dependency project '"
								+ dependency.getProjectPath() + "': invalid access token");
					}
					subject = accessToken.asSubject();
				} else {
					subject = SecurityUtils.asAnonymous();
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
							+ dependencyBuild.getReference().toString(null) + "': permission denied");
				}
				
				if (build.getDependencies().stream()
						.anyMatch(it -> it.getDependency().equals(dependencyBuild))) {
					throw new ExplicitException("Duplicate dependency build '"
							+ dependencyBuild.getReference().toString(null) + "'");
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
			FileUtils.cleanDir(buildManager.getBuildDir(projectId, buildNumber));
			return null;
		});
		listenerRegistry.post(new BuildSubmitted(build));
	}

	private boolean isApplicable(Build build, JobExecutor executor) {
		if (!build.getJob().getRequiredServices().isEmpty() && !(executor instanceof DockerAware))
			return false;
			
		for (var step: build.getJob().getSteps()) {
			if (!step.isApplicable(build, executor)) 
				return false;
		}
		if (executor.getJobMatch() != null) {
			JobMatch jobMatch = JobMatch.parse(executor.getJobMatch(), true, true);
			PullRequest request = build.getRequest();
			if (request != null) {
				if (request.getSource() != null) {
					JobMatchContext sourceContext = new JobMatchContext(
							request.getSourceProject(), request.getSourceBranch(),
							null, request.getSubmitter(), build.getJobName());
					JobMatchContext targetContext = new JobMatchContext(
							request.getTargetProject(), request.getTargetBranch(),
							null, request.getSubmitter(), build.getJobName());
					return jobMatch.matches(sourceContext) && jobMatch.matches(targetContext);
				} else {
					return false;
				}
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
				else if (!isApplicable(build, jobExecutor))
					throw new ExplicitException("Specified job executor '" + jobExecutorName + "' is not applicable for current job");
				else 
					return jobExecutor;
			} else {
				throw new ExplicitException("Unable to find specified job executor '" + jobExecutorName + "'");
			}
		} else {
			if (!settingManager.getJobExecutors().isEmpty()) {
				for (var executor : settingManager.getJobExecutors()) {
					if (executor.isEnabled() && isApplicable(build, executor))
						return executor;
				}
				throw new ExplicitException("No applicable job executor");
			} else {
				jobLogger.log("No job executor defined, auto-discovering...");
				List<JobExecutorDiscoverer> discoverers = new ArrayList<>(OneDev.getExtensions(JobExecutorDiscoverer.class));
				discoverers.sort(Comparator.comparing(JobExecutorDiscoverer::getOrder));
				for (var discoverer : discoverers) {
					JobExecutor jobExecutor = discoverer.discover();
					if (jobExecutor != null) {
						jobExecutor.setName("auto-discovered");
						if (isApplicable(build, jobExecutor)) {
							jobLogger.log("Discovered " + EditableUtils.getDisplayName(jobExecutor.getClass()).toLowerCase());
							return jobExecutor;
						}
					}
				}
				throw new ExplicitException("No applicable job executor discovered for current job");
			}
		}
	}

	private Future<Boolean> execute(Build build) {		
		String jobToken = build.getJobToken();
		VariableInterpolator interpolator = new VariableInterpolator(build, build.getParamCombination());

		TaskLogger jobLogger = logManager.newLogger(build);
		String jobExecutorName = interpolator.interpolate(build.getJob().getJobExecutor());
		JobExecutor jobExecutor = interpolator.interpolateProperties(getJobExecutor(build, jobExecutorName, jobLogger));
		String sequentialGroup = interpolator.interpolate(build.getJob().getSequentialGroup());
		String sequentialKey;
		if (sequentialGroup != null)
			sequentialKey = jobExecutorName + ":" + sequentialGroup;
		else
			sequentialKey = null;
		Long projectId = build.getProject().getId();
		String projectPath = build.getProject().getPath();
		String projectGitDir = projectManager.getGitDir(build.getProject().getId()).getAbsolutePath();
		Long buildId = build.getId();
		Long buildNumber = build.getNumber();
		Long buildSequence = build.getSubmitSequence();
		String refName = build.getRefName();
		ObjectId commitId = ObjectId.fromString(build.getCommitHash());
		BuildSpec buildSpec = build.getSpec();

		List<ServiceFacade> services = new ArrayList<>();
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

			for (String serviceName : job.getRequiredServices()) {
				Service service = buildSpec.getServiceMap().get(serviceName);
				services.add(interpolator.interpolateProperties(service).getFacade(build, jobToken));
			}
			
			timeout = job.getTimeout() * 1000L;
		} finally {
			Build.pop();
			JobAuthorizationContext.pop();
		}

		JobContext jobContext = new JobContext(jobToken, jobExecutor, projectId, projectPath,
				projectGitDir, buildId, buildNumber, buildSequence, actions, refName, commitId,
				services, timeout, build.getSecretMasker());
		
		return executorService.submit(() -> {
			int retried = 0;
			while (true) {
				long beginTime = System.currentTimeMillis();
				if (sequentialKey != null) {
					jobLogger.log("Locking sequential group...");
					while (true) {
						if (sequentialKeys.putIfAbsent(sequentialKey, new Date(), timeout, TimeUnit.MILLISECONDS) != null)
							Thread.sleep(1000);
						else
							break;
					}
				}
				// Store original job actions as the copy in job context will be fetched from cluster and 
				// some transient fields (such as step object in ServerSideFacade) will not be preserved 
				jobActions.put(jobToken, actions);
				logManager.addJobLogger(jobToken, jobLogger);
				serverStepThreads.put(jobToken, new ArrayList<>());
				try {
					var future = executorService.submit(() -> jobExecutor.execute(jobContext, jobLogger));
					Throwable throwable = null;
					try {
						var waitTime = timeout - (System.currentTimeMillis() - beginTime);
						if (waitTime > 0) {
							if (future.get(waitTime, TimeUnit.MILLISECONDS))
								return true;
						} else {
							future.cancel(true);
							throwable = new TimeoutException();
						}
					} catch (Throwable t) {
						if (!future.isDone())
							future.cancel(true);
						if (t instanceof InterruptedException)
							throw t;
						else
							throwable = t;
					}
					
					if (!checkRetry(job, jobContext, jobLogger, throwable, retried)) {
						if (throwable != null)
							throw ExceptionUtils.unchecked(throwable);
						else
							return false;
					} else {
						retried++;
					}
				} finally {
					Collection<Thread> threads = serverStepThreads.remove(jobToken);
					synchronized (threads) {
						for (Thread thread : threads)
							thread.interrupt();
					}
					logManager.removeJobLogger(jobToken);
					jobActions.remove(jobToken);
					
					if (sequentialKey != null)
						sequentialKeys.remove(sequentialKey);
				}
			}
		});
	}
	
	private boolean checkRetry(Job job, JobContext jobContext, TaskLogger jobLogger,
							   @Nullable Throwable throwable, int retried) {
		if (retried < job.getMaxRetries() && sessionManager.call(() -> {
			RetryCondition retryCondition = RetryCondition.parse(job, job.getRetryCondition());
			AtomicReference<String> errorMessage = new AtomicReference<>(null);
			if (throwable != null) {
				log(throwable, new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						errorMessage.set(message);
					}

				});
			}
			return retryCondition.matches(new RetryContext(buildManager.load(jobContext.getBuildId()), errorMessage.get()));
		})) {
			if (throwable != null)
				log(throwable, jobLogger);
			jobLogger.warning("Job will be retried after a while...");
			try {
				Thread.sleep(job.getRetryDelay() * (long) (Math.pow(2, retried)) * 1000L);
			} catch (InterruptedException e2) {
				throw new RuntimeException(e2);
			}
			transactionManager.run(() -> {
				Build innerBuild = buildManager.load(jobContext.getBuildId());
				innerBuild.setRunningDate(null);
				innerBuild.setPendingDate(new Date());
				innerBuild.setRetryDate(new Date());
				innerBuild.setStatus(Status.PENDING);
				innerBuild.getCheckoutPaths().clear();
				listenerRegistry.post(new BuildRetrying(innerBuild));
				buildManager.update(innerBuild);
			});
			return true;
		} else {
			return false;
		}
	}

	private void log(Throwable e, TaskLogger logger) {
		if (find(e, TimeoutException.class) != null) {
			logger.error(TIMEOUT_MESSAGE);
		} else {
			var explicitException = find(e, ExplicitException.class);
			if (explicitException != null)
				logger.error(explicitException.getMessage());
			else
				logger.error("Error executing job", e);
		}
	}

	@Override
	public JobContext getJobContext(String jobToken, boolean mustExist) {
		var jobServer = jobServers.get(jobToken);
		if (mustExist && jobServer == null)
			throw new ExplicitException("No job context found for specified job token");
		if (jobServer != null) {
			var jobContext = clusterManager.runOnServer(jobServer, () -> jobContexts.get(jobToken));
			if (mustExist && jobContext == null)
				throw new ExplicitException("No job context found for specified job token");
			return jobContext;
		} else {
			return null;
		}
	}

	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.FAILED);
		logManager.newLogger(build).error(errorMessage);
		build.setFinishDate(new Date());
		buildManager.update(build);
		listenerRegistry.post(new BuildFinished(build));
	}

	@Listen
	public void on(ProjectDeleted event) {
		var keysToRemove = new HashSet<String>();
		for (var key: branchSchedules.keySet()) {
			if (key.startsWith(event.getProjectId() + ":"))
				keysToRemove.add(key);
		}
		for (var key: keysToRemove)
			branchSchedules.remove(key);
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware && ((CommitAware) event).getCommit() != null) {
			ObjectId commitId = ((CommitAware) event).getCommit().getCommitId();
			if (!commitId.equals(ObjectId.zeroId())) {
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
								var paramMaps = resolveParams(null, null, 
										match.getParamMatrix(), match.getExcludeParamMaps());
								Long projectId = event.getProject().getId();

								// run asynchrously as session may get closed due to exception
								sessionManager.runAsyncAfterCommit(new Runnable() {

									@Override
									public void run() {
										SecurityUtils.bindAsSystem();
										Project project = projectManager.load(projectId);
										try {
											for (var paramMap: paramMaps) {
												submit(project, commitId, job.getName(), paramMap,
														match.getRefName(), SecurityUtils.getUser(),
														match.getRequest(), match.getIssue(), match.getReason());
											}
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
					var errorMessage = String.format(
							"Job not found (project: %s, commit: %s, job: %s)",
							build.getProject().getPath(), build.getCommitId().name(), build.getJobName());
					throw new HttpResponseAwareException(SC_BAD_REQUEST, errorMessage);
				}

				build.setStatus(Build.Status.WAITING);
				build.setSubmitSequence(build.getSubmitSequence()+1);
				build.setJobToken(UUID.randomUUID().toString());
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
					checkNotNull(paramSpec);
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
			} catch (ValidationException e) {
				throw new HttpResponseAwareException(SC_BAD_REQUEST, e.getMessage());
			} finally {
				JobAuthorizationContext.pop();
			}

			for (BuildDependence dependence : build.getDependencies()) {
				Build dependency = dependence.getDependency();
				if (dependence.isRequireSuccessful() && !dependency.isSuccessful())
					resubmit(dependency, "Resubmitted by dependent build");
			}
		} else {
			throw new HttpResponseAwareException(SC_NOT_ACCEPTABLE, "Build #" + build.getNumber() + " not finished yet");
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

	@Sessional
	@Override
	public WebShell openShell(Build build, Terminal terminal) {
		JobContext jobContext = getJobContext(build.getId());
		if (jobContext != null) {
			String jobToken = jobContext.getJobToken();
			String shellServer = jobServers.get(jobToken);
			if (shellServer != null) {
				if (SecurityUtils.canOpenTerminal(build)) {
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

					return new WebShell(build.getId(), terminal.getSessionId()) {

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
			var future = jobFutures.get(buildId);
			if (future != null) {
				future.cancel(true);
				transactionManager.run(() -> {
					Build innerBuild = buildManager.load(buildId);
					innerBuild.setCanceller(userManager.load(userId));
					buildManager.update(innerBuild);
				});
			}
			return null;
		});
	}

	@Listen
	public void on(SystemStarting event) {
		var hazelcastInstance = clusterManager.getHazelcastInstance();
		jobServers = hazelcastInstance.getMap("jobServers");
		sequentialKeys = hazelcastInstance.getMap("seqentialKeys");
		branchSchedules = hazelcastInstance.getReplicatedMap("branchSchedules");
	}
	
	private void cacheBranchSchedules(Project project, String branch, ObjectId commitId) {
		JobAuthorizationContext.push(new JobAuthorizationContext(project, commitId, SecurityUtils.getUser(), null));
		try {
			var schedules = new ArrayList<JobSchedule>();
			if (!commitId.equals(ObjectId.zeroId())) {
				var buildSpec = project.getBuildSpec(commitId);
				if (buildSpec != null) {
					validateBuildSpec(project, commitId, buildSpec);
					var triggerEvent = new ScheduledTimeReaches(project, branch);
					for (var job : buildSpec.getJobMap().values()) {
						for (var trigger : job.getTriggers()) {
							var match = trigger.matches(triggerEvent, job);
							if (match != null) {
								var cronExpression = new CronExpression(((ScheduleTrigger)trigger).getCronExpression());
								schedules.add(new JobSchedule(commitId, job.getName(), cronExpression, match));
							}
						}
					}
				}
			}
			var key = project.getId() + ":" + branch;
			if (schedules.isEmpty())
				branchSchedules.remove(key);
			else
				branchSchedules.put(key, schedules);
		} catch (BuildSpecParseException e) {
			logger.warn("Malformed build spec (project: {}, branch: {})", project.getPath(), branch);
		} catch (Exception e) {
			logger.error(String.format("Error caching branch schedules (project: %s, branch: %s)", project.getPath(), branch), e);
		} finally {
			JobAuthorizationContext.pop();
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		workExecutor.submit(CACHE_SCHEDULE_PRIORITY, () -> {
			sessionManager.run(() -> {
				for (var projectId : projectManager.getActiveIds()) {
					var project = projectManager.load(projectId);
					var repository = projectManager.getRepository(projectId);
					try {
						for (var ref : repository.getRefDatabase().getRefsByPrefix(R_HEADS)) {
							var branch = GitUtils.ref2branch(ref.getName());
							cacheBranchSchedules(project, branch, ref.getObjectId());
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}				
			});
		});
		
		maintenanceTaskId = taskScheduler.schedule(new SchedulableTask() {
			
			@Override
			public void execute() {
				batchWorkManager.submit(new BatchWorker("job-manager-maintenance") {

					@Override
					public void doWorks(List<Prioritized> works) {
						if (clusterManager.isLeaderServer()) {
							var activeJobTokens = getActiveJobTokens();
							jobServers.removeAll(it -> !activeJobTokens.contains(it.getKey()));
						}
					}
					
				}, new Prioritized(MAINTENANCE_PRIORITY));
			}

			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
			}
			
		});
		
		branchSchedulesTaskId = taskScheduler.schedule(new SchedulableTask() {
			@Override
			public void execute() {
				if (thread != null) {
					sessionManager.run(() -> {
						SecurityUtils.bindAsSystem();
						var currentTime = new Date();
						var nextCheckTime = new DateTime(currentTime.getTime()).plusMinutes(1).toDate();
						var activeProjectIds = projectManager.getActiveIds();
						for (var entry: branchSchedules.entrySet()) {
							var projectId = Long.valueOf(StringUtils.substringBefore(entry.getKey(), ":"));
							if (activeProjectIds.contains(projectId)) {
								Project project = projectManager.load(projectId);
								for (var schedule: entry.getValue()) {
									var match = schedule.getMatch();
									try {
										var commitId = schedule.getCommitId();
										var nextFireTime = schedule.getCronExpression().getNextValidTimeAfter(currentTime);
										if (nextFireTime != null && !nextFireTime.after(nextCheckTime)) {
											var paramMaps = resolveParams(null, null,
													match.getParamMatrix(), match.getExcludeParamMaps());
											for (var paramMap : paramMaps) {
												var build = submit(project, commitId, schedule.getJobName(), paramMap,
														match.getRefName(), SecurityUtils.getUser(), null,
														null, match.getReason());
												if (build.isFinished()) 
													resubmit(build, match.getReason());
											}
										}
									} catch (Exception e) {
										String errorMessage = String.format("Error triggering scheduled job (project: %s, branch: %s)",
												project.getPath(), GitUtils.ref2branch(match.getRefName()));
										logger.error(errorMessage, e);
									}
								}
							}
						}
					});
				}
			}

			@Override
			public ScheduleBuilder<?> getScheduleBuilder() {
				return SimpleScheduleBuilder.repeatMinutelyForever();
			}
			
		});
		
		thread = new Thread(this);
		thread.start();
	}

	@Sessional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		if (branch != null) {
			Project project = event.getProject();
			cacheBranchSchedules(project, branch, event.getNewCommitId());
		}
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
		if (branchSchedulesTaskId != null)
			taskScheduler.unschedule(branchSchedulesTaskId);
		if (maintenanceTaskId != null)
			taskScheduler.unschedule(maintenanceTaskId);
	}

	@Override
	public void run() {
		while (!jobFutures.isEmpty() || thread != null) {
			if (thread == null) {
				if (!jobFutures.isEmpty())
					logger.info("Waiting for jobs to finish...");
				for (var execution: jobFutures.values()) {
					if (!execution.isDone())
						execution.cancel(true);
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
								for (var buildId : buildIdsOfServer) {
									var build = buildManager.load(buildId);
									if (build.getStatus() == Status.WAITING) {
										if (build.getDependencies().stream().anyMatch(it -> it.isRequireSuccessful()
												&& it.getDependency().isFinished()
												&& it.getDependency().getStatus() != Status.SUCCESSFUL)) {
											markBuildError(build, "Some dependencies are required to be successful but failed");
										} else if (build.getDependencies().stream().allMatch(it -> it.getDependency().isFinished())) {
											build.setStatus(Status.PENDING);
											build.setPendingDate(new Date());
											listenerRegistry.post(new BuildPending(build));
										}
									} else if (build.getStatus() == Status.PENDING) {
										var future = jobFutures.get(build.getId());
										if (future == null && thread != null) {
											try {
												jobFutures.put(build.getId(), execute(build));
											} catch (Throwable t) {
												ExplicitException explicitException = find(t, ExplicitException.class);
												if (explicitException != null)
													markBuildError(build, explicitException.getMessage());
												else
													markBuildError(build, Throwables.getStackTraceAsString(t));
											}
										}
									} else if (build.getStatus() == Status.RUNNING) {
										if (jobFutures.get(build.getId()) == null) {
											build.setRunningDate(null);
											build.setPendingDate(new Date());
											build.setRetryDate(null);
											build.getCheckoutPaths().clear();
											build.setStatus(Status.PENDING);
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
					for (var it = jobFutures.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry<Long, Future<Boolean>> entry = it.next();
						Build build = buildManager.get(entry.getKey());
						var future = entry.getValue();
						if (build == null || build.isFinished()) {
							it.remove();
							future.cancel(true);
						} else if (future.isDone()) {
							it.remove();
							var jobLogger = logManager.newLogger(build);
							try {
								if (future.get())
									build.setStatus(Status.SUCCESSFUL);
								else
									build.setStatus(Status.FAILED);
								jobLogger.log("Job finished");
							} catch (CancellationException e) {
								build.setStatus(Status.CANCELLED);
							} catch (Throwable t) {
								if (find(t, TimeoutException.class) != null) 
									build.setStatus(Status.TIMED_OUT);
								else 
									build.setStatus(Status.FAILED);
								log(t, jobLogger);
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
	public boolean runJob(String server, ClusterTask<Boolean> runnable) {
		Future<Boolean> future = null;
		try {
			future = clusterManager.submitToServer(server, runnable);

			// future.get() here does not respond to thread interruption
			while (!future.isDone())
				Thread.sleep(1000);
			return future.get(); 
		} catch (InterruptedException e) {
			if (future != null)
				future.cancel(true);
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean runJob(JobContext jobContext, JobRunnable runnable) {
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
					return runnable.run(jobLogger);
				} finally {
					logManager.removeJobLogger(jobToken);
				}
			} else {
				return runnable.run(jobLogger);
			}
		} finally {
			jobRunnables.remove(jobToken);
			jobContexts.remove(jobToken);
			jobServers.remove(jobToken);
		}
	}

	private String normalizeFilePath(String filePath) {
		var normalizedFilePath = filePath.replace('\\', '/');
		normalizedFilePath = Paths.get(normalizedFilePath).normalize().toString();
		normalizedFilePath = normalizedFilePath.replace('\\', '/');
		return normalizedFilePath;
	}

	@Override
	public void reportJobWorkspace(JobContext jobContext, String workspacePath) {
		transactionManager.run(() -> {
			Build build = buildManager.load(jobContext.getBuildId());
			build.setWorkspacePath(normalizeFilePath(workspacePath));
			CompositeFacade entryFacade = new CompositeFacade(jobContext.getActions());
			entryFacade.traverse((LeafVisitor<Void>) (executable, position) -> {
				if (executable instanceof CheckoutFacade) {
					CheckoutFacade checkoutFacade = (CheckoutFacade) executable;
					var checkoutPath = workspacePath;
					if (checkoutFacade.getCheckoutPath() != null)
						checkoutPath += "/" + checkoutFacade.getCheckoutPath();
					build.getCheckoutPaths().add(normalizeFilePath(checkoutPath));
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
								TarUtils.untar(is, targetDir, false);
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
	public ServerStepResult runServerStep(JobContext jobContext, List<Integer> stepPosition,
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
					return transformedServerSideStep.run(jobContext.getBuildId(), inputDir, logger);
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
	
}