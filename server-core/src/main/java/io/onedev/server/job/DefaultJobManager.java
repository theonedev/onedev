package io.onedev.server.job;

import static io.onedev.k8shelper.KubernetesHelper.BUILD_VERSION;
import static io.onedev.k8shelper.KubernetesHelper.replacePlaceholders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

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

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.agent.job.FailedException;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.CacheAllocationRequest;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.LeafFacade;
import io.onedev.k8shelper.ServerSideFacade;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecParseException;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.JobExecutorDiscoverer;
import io.onedev.server.buildspec.job.JobTriggerMatch;
import io.onedev.server.buildspec.job.SubmitReason;
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
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.ScheduledTimeReaches;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.build.BuildFinished;
import io.onedev.server.event.project.build.BuildPending;
import io.onedev.server.event.project.build.BuildRetrying;
import io.onedev.server.event.project.build.BuildRunning;
import io.onedev.server.event.project.build.BuildSubmitted;
import io.onedev.server.event.project.build.BuildUpdated;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.job.authorization.JobAuthorization;
import io.onedev.server.job.authorization.JobAuthorization.Context;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.LogTask;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.code.CodeIndexManager;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.WebShell;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.JobSecretAuthorizationContext;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.interpolative.VariableInterpolator;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.web.editable.EditableStringTransformer;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.annotation.Interpolative;

@Singleton
public class DefaultJobManager implements JobManager, Runnable, CodePullAuthorizationSource, Serializable {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);
	
	private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();
	
	private final Map<String, Collection<Thread>> serverStepThreads = new ConcurrentHashMap<>();
	
	private final Map<String, List<Action>> jobActions = new ConcurrentHashMap<>();
	
	private final Map<String, JobExecutor> jobExecutors = new ConcurrentHashMap<>();
	
	private final Map<Long, Collection<String>> scheduledTasks = new ConcurrentHashMap<>();
	
	private final Map<String, Shell> jobShells = new ConcurrentHashMap<>();
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final LogManager logManager;
	
	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	private final ExecutorService executorService;
	
	private final BuildParamManager buildParamManager;
	
	private final AgentManager agentManager;
	
	private final TaskScheduler taskScheduler;
	
	private final Validator validator;
	
	private final ClusterManager clusterManager;
	
	private final CodeIndexManager codeIndexManager;
	
	private final StorageManager storageManager;
	
	private final ResourceAllocator resourceAllocator;
	
	private final GitService gitService;
	
	private volatile Thread thread;
	
	private volatile Map<String, JobContext> jobContexts;
	
	private volatile Map<String, UUID> jobServers;
	
	private volatile Map<String, Collection<String>> allocatedCaches;
	
	@Inject
	public DefaultJobManager(BuildManager buildManager, UserManager userManager, ListenerRegistry listenerRegistry, 
			SettingManager settingManager, TransactionManager transactionManager, LogManager logManager, 
			ExecutorService executorService, SessionManager sessionManager, BuildParamManager buildParamManager, 
			ProjectManager projectManager, Validator validator, TaskScheduler taskScheduler, AgentManager agentManager, 
			ClusterManager clusterManager, CodeIndexManager codeIndexManager, StorageManager storageManager,
			ResourceAllocator resourceAllocator, PullRequestManager pullRequestManager, GitService gitService) {
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
		this.agentManager = agentManager;
		this.codeIndexManager = codeIndexManager;
		this.clusterManager = clusterManager;
		this.resourceAllocator = resourceAllocator;
		this.storageManager = storageManager;
		this.pullRequestManager = pullRequestManager;
		this.gitService = gitService;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(JobManager.class);
	}
	
	private void validateBuildSpec(Project project, ObjectId commitId, BuildSpec buildSpec) {
		Project.push(project);
		try {
	    	for (ConstraintViolation<?> violation: validator.validate(buildSpec)) {
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
			Map<String, List<String>> paramMap, String pipeline, SubmitReason reason) {
    	Lock lock = LockUtils.getLock("job-manager: " + project.getId() + "-" + commitId.name());
    	transactionManager.mustRunAfterTransaction(new Runnable() {

			@Override
			public void run() {
				lock.unlock();
			}
    		
    	});
    	
    	JobSecretAuthorizationContext.push(new JobSecretAuthorizationContext(project, commitId, reason.getPullRequest()));
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
        	
			return doSubmit(project, commitId, jobName, paramMap, pipeline, reason); 
    	} catch (Throwable e) {
    		throw ExceptionUtils.unchecked(e);
		} finally {
			JobSecretAuthorizationContext.pop();
		}
	}
	
	private Build doSubmit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, String pipeline, SubmitReason reason) {
		ScriptIdentity.push(new JobIdentity(project, commitId));
		try {
			Build build = new Build();
			build.setProject(project);
			build.setCommitHash(commitId.name());
			build.setJobName(jobName);
			build.setSubmitDate(new Date());
			build.setStatus(Build.Status.WAITING);
			build.setSubmitReason(reason.getDescription());
			build.setSubmitter(SecurityUtils.getUser());
			build.setRefName(reason.getRefName());
			build.setRequest(reason.getPullRequest());
			build.setPipeline(pipeline);
			
			ParamUtils.validateParamMap(build.getJob().getParamSpecs(), paramMap);
			
			Map<String, List<String>> paramMapToQuery = new HashMap<>(paramMap);
			for (ParamSpec paramSpec: build.getJob().getParamSpecs()) {
				if (paramSpec instanceof SecretParam)
					paramMapToQuery.remove(paramSpec.getName());
			}
	
			Collection<Build> builds = buildManager.query(project, commitId, jobName, 
					reason.getRefName(), Optional.ofNullable(reason.getPullRequest()), 
					paramMapToQuery, pipeline);
			
			if (builds.isEmpty()) {
				for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) {
					ParamSpec paramSpec = Preconditions.checkNotNull(build.getJob().getParamSpecMap().get(entry.getKey()));
					if (!entry.getValue().isEmpty()) {
						for (String string: entry.getValue()) {
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
				for (JobDependency dependency: build.getJob().getJobDependencies()) {
					JobDependency interpolated = interpolator.interpolateProperties(dependency);
					new MatrixRunner<List<String>>(ParamUtils.getParamMatrix(build, build.getParamCombination(), 
							interpolated.getJobParams())) {
						
						@Override
						public void run(Map<String, List<String>> paramMap) {
							Build dependencyBuild = doSubmit(project, commitId, 
									interpolated.getJobName(), paramMap, pipeline, reason);
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
				
				for (ProjectDependency dependency: build.getJob().getProjectDependencies()) {
					dependency = interpolator.interpolateProperties(dependency);
					Project dependencyProject = projectManager.findByPath(dependency.getProjectPath());
					if (dependencyProject == null)
						throw new ExplicitException("Unable to find dependency project: " + dependency.getProjectPath());
	
					Subject subject;
					if (dependency.getAccessTokenSecret() != null) {
						String accessToken = build.getJobSecretAuthorizationContext().getSecretValue(dependency.getAccessTokenSecret());
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
				Long pullRequestId = PullRequest.idOf(reason.getPullRequest());
				sessionManager.runAsyncAfterCommit(new Runnable() {

					@Override
					public void run() {
						SecurityUtils.bindAsSystem();
						Project project = projectManager.load(projectId);
						PullRequest pullRequest;
						if (pullRequestId != null)
							pullRequest = pullRequestManager.load(pullRequestId);
						else
							pullRequest = null;
						for (Build unfinished: buildManager.queryUnfinished(project, jobName, reason.getRefName(), 
								Optional.ofNullable(pullRequest), paramMapToQuery)) {
							if (unfinished.getId() < buildId 
									&& gitService.isMergedInto(project, null, unfinished.getCommitId(), commitId)) { 
								cancel(unfinished);
							}
						}
					}
					
				});
				
				return build;
			} else {
				return builds.iterator().next();
			}
		} finally {
			ScriptIdentity.pop();
		}
	}
	
	private void buildSubmitted(Build build) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				FileUtils.cleanDir(storageManager.getBuildDir(projectId, buildNumber));
				return null;
			}
			
		});
		listenerRegistry.post(new BuildSubmitted(build));
	}
	
	private boolean isAuthorized(JobExecutor executor, Build build) {
		if (executor.getJobAuthorization() != null) {
			JobAuthorization authorization = JobAuthorization.parse(executor.getJobAuthorization());
			Collection<ObjectId> descendants = OneDev.getInstance(CommitInfoManager.class)
					.getDescendants(build.getProject().getId(), Sets.newHashSet(build.getCommitId()));
			descendants.add(build.getCommitId());
		
			for (RefFacade ref: build.getProject().getBranchRefs()) {
				if (descendants.contains(ref.getPeeledObj())) {
					String branch = Preconditions.checkNotNull(GitUtils.ref2branch(ref.getName()));
					if (authorization.matches(new Context(build.getProject(), branch, build.getJobName()))) 
						return true;
				}
			}
			PullRequest request = build.getRequest();
			return request != null 
					&& request.getSource() != null 
					&& authorization.matches(new Context(request.getTargetProject(), request.getTargetBranch(), build.getJobName())) 
					&& authorization.matches(new Context(request.getSourceProject(), request.getSourceBranch(), build.getJobName()));
		} else {
			return true;
		}
	}
	
	private JobExecutor getJobExecutor(Build build, @Nullable String jobExecutorName, TaskLogger jobLogger) {
		if (StringUtils.isNotBlank(jobExecutorName)) {
			JobExecutor jobExecutor = null;
			for (JobExecutor each: settingManager.getJobExecutors()) {
				if (each.getName().equals(jobExecutorName)) {
					jobExecutor = each;
					break;
				} 
			}
			if (jobExecutor != null) {
				if (!jobExecutor.isEnabled())
					throw new ExplicitException("Specified job executor '" + jobExecutorName + "' is disabled");
				else if (!isAuthorized(jobExecutor, build))
					throw new ExplicitException("Specified job executor '" + jobExecutorName + "' is not authorized for current job");
				else
					return jobExecutor;
			} else {
				throw new ExplicitException("Unable to find specified job executor '" + jobExecutorName + "'");
			}
		} else {
			if (!settingManager.getJobExecutors().isEmpty()) { 
				for (JobExecutor executor: settingManager.getJobExecutors()) {
					if (executor.isEnabled() && isAuthorized(executor, build))
						return executor;
				}
				throw new ExplicitException("No authorized job executor");
			} else {
				jobLogger.log("No job executor defined, auto-discovering...");
				List<JobExecutorDiscoverer> discoverers = new ArrayList<>(OneDev.getExtensions(JobExecutorDiscoverer.class));
				discoverers.sort(Comparator.comparing(JobExecutorDiscoverer::getOrder));
				for (JobExecutorDiscoverer discoverer: discoverers) {
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
		
		Collection<String> jobSecretsToMask = Sets.newHashSet(jobToken, clusterManager.getCredentialValue());
		TaskLogger jobLogger = logManager.newLogger(build, jobSecretsToMask);
		String jobExecutorName = interpolator.interpolate(build.getJob().getJobExecutor());
		
		JobExecutor jobExecutor = getJobExecutor(build, jobExecutorName, jobLogger);
		if (jobExecutor != null) {
			Long projectId = build.getProject().getId();
			String projectPath = build.getProject().getPath();
			String projectGitDir = storageManager.getProjectGitDir(build.getProject().getId()).getAbsolutePath();
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
			
			Job job;
			
			JobSecretAuthorizationContext.push(build.getJobSecretAuthorizationContext());
			Build.push(build);
			try {
				job = build.getJob();
				
				for (Step step: job.getSteps()) {
					step = interpolator.interpolateProperties(step);
					actions.add(step.getAction(build, jobToken, build.getParamCombination()));
				}
				
				for (CacheSpec cache: job.getCaches()) 
					caches.add(interpolator.interpolateProperties(cache));
				
				for (String serviceName: job.getRequiredServices()) {
					Service service = buildSpec.getServiceMap().get(serviceName);
					services.add(interpolator.interpolateProperties(service));
				}
				
				maxRetries.set(job.getMaxRetries());
				retryDelay.set(job.getRetryDelay());
			} finally {
				Build.pop();
				JobSecretAuthorizationContext.pop();
			}

			int cpuRequirement = 0, memoryRequirement = 0;
			cpuRequirement = job.getCpuRequirement();
			memoryRequirement = job.getMemoryRequirement();
			
			for (String serviceName: job.getRequiredServices()) {
				Service service = buildSpec.getServiceMap().get(serviceName);
				if (service != null) {
					cpuRequirement += service.getCpuRequirement();
					memoryRequirement += service.getMemoryRequirement();
				}
			}
			Map<String, Integer> resourceRequirements = new HashMap<>();
			resourceRequirements.put(ResourceAllocator.CPU, cpuRequirement);
			resourceRequirements.put(ResourceAllocator.MEMORY, memoryRequirement);

			AtomicReference<JobExecution> executionRef = new AtomicReference<>(null);
			executionRef.set(new JobExecution(executorService.submit(new Runnable() {

				@Override
				public void run() {
					AtomicInteger retried = new AtomicInteger(0);
					while (true) {
						JobContext jobContext = new JobContext(jobToken, jobExecutor, projectId, projectPath, 
								projectGitDir, buildId, buildNumber, actions, refName, commitId, caches, services, 
								resourceRequirements, retried.get());
						// Store original job actions as the copy in job context will be fetched from cluster and 
						// some transient fields (such as step object in ServerSideFacade) will not be preserved 
						jobActions.put(jobToken, actions);
						jobContexts.put(jobToken, jobContext);
						logManager.addJobLogger(jobToken, jobLogger);
						serverStepThreads.put(jobToken, new ArrayList<>());
						try {
							jobLogger.log("Waiting for resources...");
							resourceAllocator.run(
									new JobRunnable(jobToken), jobExecutor.getAgentRequirement(), resourceRequirements);
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
								transactionManager.run(new Runnable() {

									@Override
									public void run() {
										Build build = buildManager.load(buildId);
										build.setRunningDate(null);
										build.setPendingDate(null);
										build.setRetryDate(new Date());
										build.setStatus(Build.Status.WAITING);
										listenerRegistry.post(new BuildRetrying(build));
										buildManager.save(build);
									}
									
								});
								try {						
									Thread.sleep(retryDelay.get() * (long)(Math.pow(2, retried.get())) * 1000L);
								} catch (InterruptedException e2) {
									throw new RuntimeException(e2);
								}
								transactionManager.run(new Runnable() {

									@Override
									public void run() {
										JobExecution execution = executionRef.get();
										if (execution != null)
											execution.updateBeginTime();
										Build build = buildManager.load(buildId);
										build.setPendingDate(new Date());
										build.setStatus(Build.Status.PENDING);
										listenerRegistry.post(new BuildPending(build));
										buildManager.save(build);
									}
									
								});
							} else {
								throw ExceptionUtils.unchecked(e);
							}
						} finally {
							Collection<Thread> threads = serverStepThreads.remove(jobToken);
							synchronized (threads) {
								for (Thread thread: threads)
									thread.interrupt();
							}
							logManager.removeJobLogger(jobToken);
							jobContexts.remove(jobToken);
							jobActions.remove(jobToken);
						}
					}							
				}			
				
			}), job.getTimeout()*1000L));
			
			return executionRef.get();
		} else {
			throw new ExplicitException("No applicable job executor");
		}
	}
	
	private void log(Throwable e, TaskLogger logger) {
		if (e instanceof ExplicitException)
			logger.error(e.getMessage());
		else
			logger.error("Exception catched", e);
	}
	
	@Override
	public JobContext getJobContext(String jobToken, boolean mustExist) {
		JobContext jobContext = jobContexts.get(jobToken);
		if (mustExist && jobContext == null)
			throw new ExplicitException("No job context found for specified job token");
		return jobContext;
	}
	
	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.FAILED);
		logManager.newLogger(build).error(errorMessage);
		build.setFinishDate(new Date());
		buildManager.save(build);
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware) {
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
				JobSecretAuthorizationContext.push(new JobSecretAuthorizationContext(event.getProject(), commitId, request));
				ScriptIdentity.push(new JobIdentity(event.getProject(), commitId));
				try {
					BuildSpec buildSpec = event.getProject().getBuildSpec(commitId);
					if (buildSpec != null) {
						validateBuildSpec(event.getProject(), commitId, buildSpec);
						for (Job job: buildSpec.getJobMap().values()) {
							JobTriggerMatch match = job.getTriggerMatch(event);
							if (match != null) {
								Map<String, List<List<String>>> paramMatrix = 
										ParamUtils.getParamMatrix(null, null, match.getTrigger().getParams());						
								Long projectId = event.getProject().getId();
								
								// run asynchrously as session may get closed due to exception
								sessionManager.runAsyncAfterCommit(new Runnable() {

									@Override
									public void run() {
										SecurityUtils.bindAsSystem();
										Project project = projectManager.load(projectId);
										try {
											new MatrixRunner<List<String>>(paramMatrix) {
												
												@Override
												public void run(Map<String, List<String>> paramMap) {
													submit(project, commitId, job.getName(), paramMap, 
															pipeline, match.getReason()); 
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
					ScriptIdentity.pop();
					JobSecretAuthorizationContext.pop();
				}
			}
		}
	}
	
	@Transactional
	@Override
	public void resubmit(Build build, String reason) {
		if (build.isFinished()) {
			JobSecretAuthorizationContext.push(build.getJobSecretAuthorizationContext());
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
				
				buildParamManager.deleteParams(build);
				for (Map.Entry<String, List<String>> entry: build.getParamMap().entrySet()) {
					ParamSpec paramSpec = build.getJob().getParamSpecMap().get(entry.getKey());
					Preconditions.checkNotNull(paramSpec);
					String type = paramSpec.getType();
					List<String> values = entry.getValue();
					if (!values.isEmpty()) {
						for (String value: values) {
							BuildParam param = new BuildParam();
							param.setBuild(build);
							param.setName(entry.getKey());
							param.setType(type);
							param.setValue(value);
							build.getParams().add(param);
							buildParamManager.save(param);
						}
					} else {
						BuildParam param = new BuildParam();
						param.setBuild(build);
						param.setName(paramSpec.getName());
						param.setType(type);
						build.getParams().add(param);
						buildParamManager.save(param);
					}
				}
				buildManager.save(build);
				buildSubmitted(build);
			} finally {
				JobSecretAuthorizationContext.pop();
			}
			
			for (BuildDependence dependence: build.getDependencies()) {
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
			UUID jobServerUUID = jobServers.get(jobContext.getJobToken());
			if (jobServerUUID != null) {
				clusterManager.runOnServer(jobServerUUID, new ClusterTask<Void>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Void call() throws Exception {
						JobContext jobContext = getJobContext(buildId);
						if (jobContext != null) {
							JobExecutor jobExecutor = jobExecutors.get(jobContext.getJobToken());
							if (jobExecutor != null)
								jobExecutor.resume(jobContext);
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
		if (jobContext!= null) {
			String jobToken = jobContext.getJobToken();
			UUID shellServerUUID = jobServers.get(jobToken);
			if (shellServerUUID != null) {
				if (SecurityUtils.isAdministrator() || jobContext.getJobExecutor().isShellAccessEnabled()) {  
					clusterManager.runOnServer(shellServerUUID, new ClusterTask<Void>() {

						private static final long serialVersionUID = 1L;

						@Override
						public Void call() throws Exception {
							JobContext jobContext = getJobContext(jobToken, true);
							JobExecutor jobExecutor = jobExecutors.get(jobContext.getJobToken());
							if (jobExecutor != null) {
								Shell shell = jobExecutor.openShell(jobContext, terminal);
								jobShells.put(terminal.getSessionId(), shell);
							} else {
								throw new ExplicitException("Job shell not ready");
							}
							return null;
						}
						
					});
					
					return new WebShell(buildId, terminal.getSessionId()) {
						
						private static final long serialVersionUID = 1L;

						@Override
						public void sendInput(String input) {
							clusterManager.submitToServer(shellServerUUID, new ClusterTask<Void>() {

								private static final long serialVersionUID = 1L;

								@Override
								public Void call() throws Exception {
									Shell shell = jobShells.get(terminal.getSessionId());
									if (shell != null)
										shell.sendInput(input);
									return null;
								}
								
							});
						}

						@Override
						public void resize(int rows, int cols) {
							clusterManager.submitToServer(shellServerUUID, new ClusterTask<Void>() {

								private static final long serialVersionUID = 1L;

								@Override
								public Void call() throws Exception {
									Shell shell = jobShells.get(terminal.getSessionId());
									if (shell != null)
										shell.resize(rows, cols);
									return null;
								}
								
							});
						}

						@Override
						public void exit() {
							clusterManager.submitToServer(shellServerUUID, new ClusterTask<Void>() {

								private static final long serialVersionUID = 1L;

								@Override
								public Void call() throws Exception {
									Shell shell = jobShells.remove(terminal.getSessionId());
									if (shell != null)
										shell.exit();
									return null;
								}
								
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
	public Shell getShellLocal(String sessionId) {
		return jobShells.get(sessionId);
	}
	
	@Override
	public JobContext getJobContext(Long buildId) {
		for (Map.Entry<String, JobContext> entry: jobContexts.entrySet()) {
			JobContext jobContext = entry.getValue();
			if (jobContext.getBuildId().equals(buildId)) 
				return jobContext;
		}
		return null;
	}

	@Transactional
	@Override
	public void cancel(Build build) {
		Long projectId = build.getProject().getId();
		Long buildId = build.getId();
		Long userId = User.idOf(SecurityUtils.getUser());
		projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				JobExecution execution = jobExecutions.get(buildId);
				if (execution != null) {
					execution.cancel(userId);
				} else {
					transactionManager.run(new Runnable() {

						@Override
						public void run() {
							Build build = buildManager.load(buildId);
							if (!build.isFinished()) {
								build.setStatus(Status.CANCELLED);
								build.setFinishDate(new Date());
								build.setCanceller(userManager.load(userId));
								buildManager.save(build);
								listenerRegistry.post(new BuildFinished(build));
							}
						}
						
					});
				}
				return null;
			}
			
		});
	}
	
	@Sessional
	@Listen(100000)
	public void on(SystemStarted event) {
		jobContexts = clusterManager.getHazelcastInstance().getMap("jobContexts");
		allocatedCaches = clusterManager.getHazelcastInstance().getMap("allocatedCaches");
		jobServers = clusterManager.getHazelcastInstance().getMap("jobRunServers");
		
		thread = new Thread(this);
		thread.start();	
	}
	
	@Listen
	public void on(DefaultBranchChanged event) {
		schedule(event.getProject());
	}
	
	@Sessional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		Project project = event.getProject();
		if (branch != null && branch.equals(project.getDefaultBranch()) && !event.getNewCommitId().equals(ObjectId.zeroId()))
			schedule(project);
	}
	
	@Sessional
	@Override
	public void schedule(Project project) {
		Collection<String> tasksOfProject = new HashSet<>();
		try {
			String defaultBranch = project.getDefaultBranch();
			if (defaultBranch != null) {
				ObjectId commitId = project.getObjectId(defaultBranch, false);
				if (commitId != null) {
					JobSecretAuthorizationContext.push(new JobSecretAuthorizationContext(project, commitId, null));
					try {
						BuildSpec buildSpec = project.getBuildSpec(commitId);
						if (buildSpec != null) {
							validateBuildSpec(project, commitId, buildSpec);
							ScheduledTimeReaches event = new ScheduledTimeReaches(project);
							for (Job job: buildSpec.getJobMap().values()) {
								for (JobTrigger trigger: job.getTriggers()) {
									if (trigger instanceof ScheduleTrigger) {
										ScheduleTrigger scheduledTrigger = (ScheduleTrigger) trigger;
										SubmitReason reason = trigger.matches(event, job);
										if (reason != null) {
											String taskId = taskScheduler.schedule(newSchedulableTask(project, commitId, job, scheduledTrigger, reason));
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
						JobSecretAuthorizationContext.pop();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error scheduling project '" + project.getPath() + "'", e);
		} finally {
			tasksOfProject = scheduledTasks.put(project.getId(), tasksOfProject);
			if (tasksOfProject != null)
				tasksOfProject.stream().forEach(it->taskScheduler.unschedule(it));
		}
	}

	@Sessional
	@Override
	public void unschedule(Project project) {
		var tasksOfProject = scheduledTasks.remove(project.getId());
		if (tasksOfProject != null)
			tasksOfProject.stream().forEach(it->taskScheduler.unschedule(it));
	}
	
	private SchedulableTask newSchedulableTask(Project project, ObjectId commitId, Job job, 
			ScheduleTrigger trigger, SubmitReason reason) {
		Long projectId = project.getId();
		return new SchedulableTask() {

			@Override
			public void execute() {
				sessionManager.run(new Runnable() {

					@Override
					public void run() {
						SecurityUtils.bindAsSystem();
						Project project = projectManager.load(projectId);
						String pipeline = UUID.randomUUID().toString();
						new MatrixRunner<List<String>>(ParamUtils.getParamMatrix(null, null, trigger.getParams())) {
							
							@Override
							public void run(Map<String, List<String>> paramMap) {
								submit(project, commitId, job.getName(), paramMap, pipeline, reason); 
							}
							
						}.run();
					}
					
				});
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
			} catch (InterruptedException e) {
			}
		}
		scheduledTasks.values().stream().forEach(it1->it1.stream().forEach(it2->taskScheduler.unschedule(it2)));
		scheduledTasks.clear();
	}

	@Override
	public void run() {
		while (!jobExecutions.isEmpty() || thread != null) {
			if (thread == null)
				logger.info("Waiting for unfinished jobs...");
			try {
				if (clusterManager.isLeaderServer()) {
					Map<UUID, Collection<Long>> buildIds = new HashMap<>();
					for (var entry: buildManager.queryUnfinished().entrySet()) {
						UUID storageServerUUID = projectManager.getStorageServerUUID(entry.getValue(), false);
						if (storageServerUUID != null) {
							Collection<Long> buildIdsOfServer = buildIds.get(storageServerUUID);
							if (buildIdsOfServer == null) {
								buildIdsOfServer = new ArrayList<>();
								buildIds.put(storageServerUUID, buildIdsOfServer);
							}
							buildIdsOfServer.add(entry.getKey());
						}
					}
					
					Collection<Future<?>> futures = new ArrayList<>();
					for (var entry: buildIds.entrySet()) {
						var buildIdsOfServer = entry.getValue();
						futures.add(clusterManager.submitToServer(entry.getKey(), new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								transactionManager.run(new Runnable() {

									@Override
									public void run() {
										for (Long buildId: buildIdsOfServer) {
											Build build = buildManager.load(buildId);
											if (build.getStatus() == Build.Status.RUNNING 
													|| build.getStatus() == Build.Status.PENDING) {
												JobExecution execution = jobExecutions.get(build.getId());
												if (execution != null) {
													if (execution.isTimedout())
														execution.cancel(null);
												} else if (thread != null) {
													build.setStatus(Build.Status.PENDING);
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
											} else if (build.getStatus() == Build.Status.WAITING) {
												if (build.getRetryDate() != null) {
													JobExecution execution = jobExecutions.get(build.getId());
													if (execution == null && thread != null) {
														build.setStatus(Build.Status.PENDING);
														build.setPendingDate(new Date());
														listenerRegistry.post(new BuildPending(build));
													}
												} else if (build.getDependencies().stream().anyMatch(it -> it.isRequireSuccessful() 
														&& it.getDependency().isFinished() 
														&& it.getDependency().getStatus() != Build.Status.SUCCESSFUL)) {
													markBuildError(build, "Some dependencies are required to be successful but failed");
												} else if (build.getDependencies().stream().allMatch(it->it.getDependency().isFinished())) {
													build.setStatus(Build.Status.PENDING);
													build.setPendingDate(new Date());
													listenerRegistry.post(new BuildPending(build));
												}
											} 
										}
									}
									
								});
								return null;
							}
							
						}));
					}
					for (var future: futures) {
						try {
							future.get();
						} catch (InterruptedException | ExecutionException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
				sessionManager.run(new Runnable() {
	
					@Override
					public void run() {
						for (Iterator<Map.Entry<Long, JobExecution>> it = jobExecutions.entrySet().iterator(); it.hasNext();) {
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
									build.setStatus(Build.Status.SUCCESSFUL);
									jobLogger.log("Job finished");
								} catch (TimeoutException e) {
									build.setStatus(Build.Status.TIMED_OUT);
								} catch (CancellationException e) {
									if (e instanceof CancellerAwareCancellationException) {
										Long cancellerId = ((CancellerAwareCancellationException) e).getCancellerId();
										if (cancellerId != null)
											build.setCanceller(userManager.load(cancellerId));
									}
									build.setStatus(Build.Status.CANCELLED);
								} catch (ExecutionException e) {
									build.setStatus(Build.Status.FAILED);
									ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
									if (explicitException != null)
										jobLogger.error(explicitException.getMessage());
									else if (ExceptionUtils.find(e, FailedException.class) == null)
										jobLogger.error("Error running job", e);
								} catch (InterruptedException e) {
								} finally {
									build.setFinishDate(new Date());
									buildManager.save(build);
									listenerRegistry.post(new BuildFinished(build));
								}
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
		JobSecretAuthorizationContext.push(build.getJobSecretAuthorizationContext());
		Build.push(build);
		try {
			VariableInterpolator interpolator = new VariableInterpolator(build, build.getParamCombination());
			Map<String, String> placeholderValues = new HashMap<>();
			placeholderValues.put(BUILD_VERSION, build.getVersion());
			if (build.getJob() != null) {
				for (PostBuildAction action: build.getJob().getPostBuildActions()) {
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
			JobSecretAuthorizationContext.pop();
		}
	}
	
	@Override
	public boolean canPullCode(HttpServletRequest request, Project project) {
		String jobToken = SecurityUtils.getBearerToken(request);
		if (jobToken != null) {
			JobContext jobContext = jobContexts.get(jobToken);					
			if (jobContext != null)
				return jobContext.getProjectId().equals(project.getId());
		}
		return false;
	}

	@Override
	public Map<CacheInstance, String> allocateCaches(JobContext jobContext, CacheAllocationRequest request) {
		UUID leaderServerUUID = clusterManager.getHazelcastInstance()
				.getCluster().getMembers().iterator().next().getUuid();
		String jobToken = jobContext.getJobToken();
		return clusterManager.runOnServer(leaderServerUUID, new ClusterTask<Map<CacheInstance, String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<CacheInstance, String> call() throws Exception {
				synchronized (allocatedCaches) {
					JobContext jobContext = getJobContext(jobToken, true);
					
					List<CacheInstance> sortedInstances = new ArrayList<>(request.getInstances().keySet());
					sortedInstances.sort(new Comparator<CacheInstance>() {
			
						@Override
						public int compare(CacheInstance o1, CacheInstance o2) {
							return request.getInstances().get(o2).compareTo(request.getInstances().get(o1));
						}
						
					});
				
					Collection<String> allAllocated = new HashSet<>();
					Collection<String> activeJobTokens = jobContexts.keySet();
					Collection<String> removeKeys = new HashSet<>();
					for (var entry: allocatedCaches.entrySet()) {
						if (activeJobTokens.contains(entry.getKey()))
							allAllocated.addAll(entry.getValue());
						else
							removeKeys.add(entry.getKey());
					}
					for (var key: removeKeys)
						allocatedCaches.remove(key);
					
					Map<CacheInstance, String> allocations = new HashMap<>();
					
					Collection<String> allocatedCachesOfJob = new ArrayList<>(); 
					for (CacheSpec cacheSpec: jobContext.getCacheSpecs()) {
						Optional<CacheInstance> result = sortedInstances
								.stream()
								.filter(it->it.getCacheKey().equals(cacheSpec.getNormalizedKey()))
								.filter(it->!allAllocated.contains(it.getName()))
								.findFirst();
						CacheInstance allocation;
						if (result.isPresent()) 
							allocation = result.get();
						else
							allocation = new CacheInstance(UUID.randomUUID().toString(), cacheSpec.getNormalizedKey());
						allocations.put(allocation, cacheSpec.getPath());
						allocatedCachesOfJob.add(allocation.getName());
						allAllocated.add(allocation.getName());
					}
					
					Consumer<CacheInstance> deletionMarker = new Consumer<CacheInstance>() {
			
						@Override
						public void accept(CacheInstance instance) {
							long ellapsed = request.getCurrentTime().getTime() - request.getInstances().get(instance).getTime();
							if (ellapsed > jobContext.getJobExecutor().getCacheTTL() * 24L * 3600L * 1000L) {
								allocations.put(instance, null);
								allocatedCachesOfJob.add(instance.getName());
								allAllocated.add(instance.getName());
							}
						}
						
					};
					
					allocatedCaches.put(jobToken, allocatedCachesOfJob);
					
					request.getInstances().keySet()
							.stream()
							.filter(it->!allAllocated.contains(it.getName()))
							.forEach(deletionMarker);
					
					return allocations;
				}
			}
			
		});
	}

	private void runJobLocal(String jobToken, AgentInfo agentInfo) {
		JobContext jobContext = getJobContext(jobToken, true);
		Long buildId = jobContext.getBuildId();
		
		transactionManager.run(new Runnable() {

			@Override
			public void run() {
				Build build = buildManager.load(buildId);
				build.setStatus(Build.Status.RUNNING);
				build.setRunningDate(new Date());
				if (agentInfo != null)
					build.setAgent(agentManager.load(agentInfo.getId()));
				buildManager.save(build);
				listenerRegistry.post(new BuildRunning(build));
			}
			
		});

		jobServers.put(jobToken, clusterManager.getLocalServerUUID());
		JobExecutor jobExecutor = jobContext.getJobExecutor();
		jobExecutors.put(jobToken, jobExecutor);
		try {
			TaskLogger jobLogger = logManager.getJobLogger(jobToken);
			if (jobLogger == null) {
				jobLogger = new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						projectManager.runOnProjectServer(jobContext.getProjectId(), new LogTask(jobToken, message, sessionId)); 
					}
					
				};
				logManager.addJobLogger(jobToken, jobLogger);
				try {
					jobExecutor.execute(jobContext, jobLogger, agentInfo);
				} finally {
					logManager.removeJobLogger(jobToken);
				}
			} else {
				jobExecutor.execute(jobContext, jobLogger, agentInfo);
			}
		} finally {
			jobExecutors.remove(jobToken);
			jobServers.remove(jobToken);
		}
	}
	
	@Override
	public void reportJobWorkspace(JobContext jobContext, String jobWorkspace) {
		transactionManager.run(new Runnable() {

			@Override
			public void run() {
				Build build = buildManager.load(jobContext.getBuildId());
				build.setJobWorkspace(jobWorkspace);
				buildManager.save(build);
			}
			
		});
	}
	
	@Sessional
	@Override
	public void copyDependencies(JobContext jobContext, File tempDir) {
		Build build = buildManager.load(jobContext.getBuildId());
		for (BuildDependence dependence: build.getDependencies()) {
			if (dependence.getArtifacts() != null) {
				Build dependency = dependence.getDependency();
				
				File targetDir;
				if (dependence.getDestinationPath() != null) {
					targetDir = new File(tempDir, dependence.getDestinationPath());
					FileUtils.createDir(targetDir);
				} else {
					targetDir = tempDir;
				}

				UUID dependencyStorageServerUUID = projectManager.getStorageServerUUID(
						dependency.getProject().getId(), true);
				if (dependencyStorageServerUUID.equals(clusterManager.getLocalServerUUID())) {
					LockUtils.read(dependency.getArtifactsLockName(), new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							File artifactsDir = dependency.getArtifactsDir();
							if (artifactsDir.exists()) {
								PatternSet patternSet = PatternSet.parse(dependence.getArtifacts());
								int baseLen = artifactsDir.getAbsolutePath().length()+1;
								for (File file: FileUtils.listFiles(artifactsDir, patternSet.getIncludes(), patternSet.getExcludes())) {
									FileUtils.copyFile(file, 
											new File(targetDir, file.getAbsolutePath().substring(baseLen)));
								}
							}
							return null;
						}
						
					});
				} else {
					String serverUrl = clusterManager.getServerUrl(dependencyStorageServerUUID);
					Client client = ClientBuilder.newClient();
					try {
						WebTarget target = client.target(serverUrl).path("~api/cluster/artifacts")
								.queryParam("projectId", dependency.getProject().getId())
								.queryParam("buildNumber", dependency.getNumber())
								.queryParam("artifacts", dependence.getArtifacts());
						Invocation.Builder builder =  target.request();
						builder.header(HttpHeaders.AUTHORIZATION, KubernetesHelper.BEARER + " " 
								+ clusterManager.getCredentialValue());
						
						try (Response response = builder.get()){
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
			File inputDir, Map<String, String> placeholderValues, TaskLogger logger) {
		UUID storageServerUUID = projectManager.getStorageServerUUID(jobContext.getProjectId(), true);
		if (storageServerUUID.equals(clusterManager.getLocalServerUUID())) {
			
			// Some steps need the commit to be indexed, for instance various 
			// report publishing steps need to query the full blob path based 
			// on a partial path (java package/class etc)
			codeIndexManager.indexAsync(jobContext.getProjectId(), jobContext.getCommitId());
			while (!codeIndexManager.isIndexed(jobContext.getProjectId(), jobContext.getCommitId())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
			return sessionManager.call(new Callable<Map<String, byte[]>>() {

				@Override
				public Map<String, byte[]> call() {
					Thread thread = Thread.currentThread();
					
					Collection<Thread> threads = serverStepThreads.get(jobContext.getJobToken());
					if (threads != null) synchronized (threads) {
						threads.add(thread);
					}
					try {
						List<Action> actions = jobActions.get(jobContext.getJobToken());
						if (actions != null) {
							ServerSideFacade serverSideFacade = (ServerSideFacade) LeafFacade.of(actions, stepPosition);
							ServerSideStep serverSideStep = (ServerSideStep) serverSideFacade.getStep();
							
							serverSideStep = new EditableStringTransformer(new Function<String, String>() {

								@Override
								public String apply(String t) {
									return replacePlaceholders(t, placeholderValues);
								}
								
							}).transformProperties(serverSideStep, Interpolative.class);

							Build build = buildManager.load(jobContext.getBuildId());
							return serverSideStep.run(build, inputDir, logger);
						} else {
							throw new IllegalStateException("Job actions not found");
						}
					} finally {
						if (threads != null) synchronized (threads) {
							threads.remove(thread);
						}
					}
					
				}
				
			});
		} else {
			String serverUrl = clusterManager.getServerUrl(storageServerUUID);
			return KubernetesHelper.runServerStep(serverUrl, jobContext.getJobToken(), stepPosition, 
					inputDir, Lists.newArrayList("**"), Lists.newArrayList(), placeholderValues, logger);
		}
	}
	
	private static class JobRunnable implements ResourceRunnable {

		private static final long serialVersionUID = 1L;

		private final String jobToken;
		
		public JobRunnable(String jobToken) {
			this.jobToken = jobToken;
		}
		
		@Override
		public void run(AgentInfo agentInfo) {
			OneDev.getInstance(DefaultJobManager.class).runJobLocal(jobToken, agentInfo);
		}
		
	}
	
}