package io.onedev.server.buildspec.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.k8shelper.CacheInstance;
import io.onedev.k8shelper.CloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.job.action.condition.ActionCondition;
import io.onedev.server.buildspec.job.log.LogManager;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramspec.SecretParam;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.buildspec.job.retrycondition.RetryCondition;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.job.trigger.ScheduleTrigger;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.ProjectCreated;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.ScheduledTimeReaches;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildPending;
import io.onedev.server.event.build.BuildRetrying;
import io.onedev.server.event.build.BuildRunning;
import io.onedev.server.event.build.BuildSubmitted;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;

@Singleton
public class DefaultJobManager implements JobManager, Runnable, CodePullAuthorizationSource {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);
	
	private final Map<String, JobContext> jobContexts = new ConcurrentHashMap<>();
	
	private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();
	
	private final Map<Long, Collection<String>> scheduledTasks = new ConcurrentHashMap<>();
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
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
	
	private volatile List<JobExecutor> jobExecutors;
	
	private volatile Thread thread;
	
	@Inject
	public DefaultJobManager(BuildManager buildManager, UserManager userManager, ListenerRegistry listenerRegistry, 
			SettingManager settingManager, TransactionManager transactionManager, LogManager logManager, 
			ExecutorService executorService, SessionManager sessionManager, BuildParamManager buildParamManager, 
			ProjectManager projectManager, Validator validator, TaskScheduler taskScheduler) {
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
	}

	private void validate(Project project, ObjectId commitId) {
		Project.push(project);
		try {
	    	for (ConstraintViolation<?> violation: validator.validate(project.getBuildSpec(commitId))) {
	    		String message = String.format("Error validating build spec (project: %s, commit: %s, property: %s, message: %s)", 
	    				project.getName(), commitId.name(), violation.getPropertyPath(), violation.getMessage());
	    		throw new ExplicitException(message);
	    	}
		} finally {
			Project.pop();
		}
	}
	
	@Transactional
	@Override
	public Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, SubmitReason reason) {
    	Lock lock = LockUtils.getLock("job-manager: " + project.getId() + "-" + commitId.name());
    	transactionManager.mustRunAfterTransaction(new Runnable() {

			@Override
			public void run() {
				lock.unlock();
			}
    		
    	});
    	
		// Lock to guarantee uniqueness of build (by project, commit, job and parameters)
    	try {
        	lock.lockInterruptibly();
        	
        	validate(project, commitId);
        	
			return submit(project, commitId, jobName, paramMap, reason, new LinkedHashSet<>()); 
    	} catch (Throwable e) {
    		throw ExceptionUtils.unchecked(e);
		}
	}
	
	private Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, SubmitReason reason, Set<String> checkedJobNames) {

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
			
			ParamSupply.validateParamMap(build.getJob().getParamSpecMap(), paramMap);
			
			if (!checkedJobNames.add(jobName)) {
				String message = String.format("Circular job dependencies found (%s)", checkedJobNames);
				throw new ExplicitException(message);
			}
	
			Map<String, List<String>> paramMapToQuery = new HashMap<>(paramMap);
			for (ParamSpec paramSpec: build.getJob().getParamSpecs()) {
				if (paramSpec instanceof SecretParam)
					paramMapToQuery.remove(paramSpec.getName());
			}
	
			Collection<Build> builds = buildManager.query(project, commitId, jobName, 
					reason.getRefName(), Optional.ofNullable(reason.getPullRequest()), 
					paramMapToQuery);
			
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
				
				for (JobDependency dependency: build.getJob().getJobDependencies()) {
					new MatrixRunner<List<String>>(ParamSupply.getParamMatrix(build, dependency.getJobParams())) {
						
						@Override
						public void run(Map<String, List<String>> params) {
							Build dependencyBuild = submit(project, commitId, dependency.getJobName(), 
									params, reason, new LinkedHashSet<>(checkedJobNames));
							BuildDependence dependence = new BuildDependence();
							dependence.setDependency(dependencyBuild);
							dependence.setDependent(build);
							dependence.setRequireSuccessful(dependency.isRequireSuccessful());
							dependence.setArtifacts(build.interpolate(dependency.getArtifacts()));
							build.getDependencies().add(dependence);
						}
						
					}.run();
				}
				
				for (ProjectDependency dependency: build.getJob().getProjectDependencies()) {
					Project dependencyProject = projectManager.find(dependency.getProjectName());
					if (dependencyProject == null)
						throw new ExplicitException("Unable to find dependency project: " + dependency.getProjectName());
	
					Subject subject;
					if (dependency.getAccessTokenSecret() != null) {
						String accessToken = build.getSecretValue(dependency.getAccessTokenSecret());
						User user = userManager.findByAccessToken(accessToken);
						if (user == null) {
							throw new ExplicitException("Unable to access dependency project '" 
									+ dependency.getProjectName() + "': invalid access token");
						}
						subject = user.asSubject();
					} else {
						subject = SecurityUtils.asSubject(0L);
					}
					String buildNumberStr = build.interpolate(dependency.getBuildNumber());
					if (buildNumberStr.startsWith("#"))
						buildNumberStr = buildNumberStr.substring(1);
					Long buildNumber = Long.parseLong(buildNumberStr);
					Build dependencyBuild = buildManager.find(dependencyProject, buildNumber);
					if (dependencyBuild == null) {
						String errorMessage = String.format("Unable to find dependency build (project: %s, build number: %d)", 
								dependency.getProjectName(), buildNumber);
						throw new ExplicitException(errorMessage);
					}
					
					JobPermission jobPermission = new JobPermission(dependencyBuild.getJobName(), new AccessBuild());
					if (!subject.isPermitted(new ProjectPermission(dependencyProject, jobPermission))) {
						throw new ExplicitException("Unable to access dependency build '" 
								+ dependencyBuild.getFQN() + "': permission denied");
					}
					
					BuildDependence dependence = new BuildDependence();
					dependence.setDependency(dependencyBuild);
					dependence.setDependent(build);
					dependence.setArtifacts(build.interpolate(dependency.getArtifacts()));
					build.getDependencies().add(dependence);
				}
	
				buildManager.create(build);
				listenerRegistry.post(new BuildSubmitted(build));
				return build;
			} else {
				return builds.iterator().next();
			}
		} finally {
			ScriptIdentity.pop();
		}
	}
	
	@Nullable
	private JobExecutor getJobExecutor(Build build) {
		for (JobExecutor executor: jobExecutors) {
			if (executor.isApplicable(build))
				return executor;
		}
		return null;
	}

	private JobExecution execute(Build build) {
		Build.push(build);
		try {
			String jobToken = UUID.randomUUID().toString();
			Collection<String> jobSecretsToMask = Sets.newHashSet(jobToken);
			
			BuildSpec buildSpec = build.getProject().getBuildSpec(build.getCommitId());
			Job job = (Job) VariableInterpolator.installInterceptor(build.getJob());
			
			JobExecutor executor = getJobExecutor(build);
			if (executor != null) {
				SimpleLogger jobLogger = logManager.getLogger(build, jobSecretsToMask); 
				
				ObjectId commitId = ObjectId.fromString(build.getCommitHash());
				Long buildId = build.getId();
				Long buildNumber = build.getNumber();
				String projectName = build.getProject().getName();
				File projectGitDir = build.getProject().getGitDir();
				CloneInfo cloneInfo = job.getCloneCredential().newCloneInfo(build, jobToken);
				
				AtomicReference<JobExecution> executionRef = new AtomicReference<>(null);
				executionRef.set(new JobExecution(executorService.submit(new Runnable() {

					@Override
					public void run() {
						jobLogger.log("Creating server workspace...");
						File serverWorkspace = FileUtils.createTempDir("server-workspace");
						try {
							Set<String> includeFiles = new HashSet<>();
							Set<String> excludeFiles = new HashSet<>();
							AtomicInteger maxRetries = new AtomicInteger(0);
							AtomicInteger retryDelay = new AtomicInteger(0);
							List<CacheSpec> caches = new ArrayList<>();
							List<JobService> services = new ArrayList<>();
							
							sessionManager.run(new Runnable() {

								@Override
								public void run() {
									Build build = buildManager.load(buildId);
									Build.push(build);
									try {
										jobLogger.log("Retrieving dependency artifacts...");
										for (BuildDependence dependence: build.getDependencies()) {
											if (dependence.getArtifacts() != null) {
												build.retrieveArtifacts(dependence.getDependency(), 
														dependence.getArtifacts(), serverWorkspace);
											}
										}

										if (job.getArtifacts() != null) {
											PatternSet patternSet = PatternSet.parse(job.getArtifacts());
											includeFiles.addAll(patternSet.getIncludes());
											excludeFiles.addAll(patternSet.getExcludes());
										}
										for (JobReport report: job.getReports()) {
											PatternSet patternSet = PatternSet.parse(report.getFilePatterns());
											includeFiles.addAll(patternSet.getIncludes());
											excludeFiles.addAll(patternSet.getExcludes());
										}
										for (CacheSpec cache: job.getCaches())
											caches.add(build.interpolateProperties(cache));
										for (JobService service: job.getServices())
											services.add(build.interpolateProperties(service));
										maxRetries.set(job.getMaxRetries());
										retryDelay.set(job.getRetryDelay());
									} finally {
										Build.pop();
									}
								}
								
							});

							AtomicInteger retried = new AtomicInteger(0);
							while (true) {
								JobContext jobContext = sessionManager.call(new Callable<JobContext> () {

									@Override
									public JobContext call() throws Exception {
										Build build = buildManager.load(buildId);
										Build.push(build);
										try {
											return new JobContext(projectName, buildNumber, projectGitDir, job.getActions(buildSpec), job.getImage(), 
													serverWorkspace, job.getCommands(), job.isRetrieveSource(), job.getCloneDepth(), 
													cloneInfo, job.getCpuRequirement(), job.getMemoryRequirement(), 
													commitId, caches, new PatternSet(includeFiles, excludeFiles), 
													executor.getCacheTTL(), retried.get(), services, jobLogger) {
												
												@Override
												public void notifyJobRunning() {
													transactionManager.run(new Runnable() {

														@Override
														public void run() {
															Build build = buildManager.load(buildId);
															build.setStatus(Build.Status.RUNNING);
															build.setRunningDate(new Date());
															buildManager.save(build);
															listenerRegistry.post(new BuildRunning(build));
														}
														
													});
												}

												@Override
												public void reportJobWorkspace(String jobWorkspace) {
													transactionManager.run(new Runnable() {

														@Override
														public void run() {
															Build build = buildManager.load(buildId);
															build.setJobWorkspace(jobWorkspace);
															buildManager.save(build);
														}
														
													});
												}
												
											};
										} finally {
											Build.pop();
										}
									}
									
								});
								
								jobContexts.put(jobToken, jobContext);
								
								try {
									executor.execute(jobToken, jobContext);
									break;
								} catch (Throwable e) {
									if (ExceptionUtils.find(e, InterruptedException.class) != null) {
										throw e;
									} else {
										if (retried.getAndIncrement() < maxRetries.get() && transactionManager.call(new Callable<Boolean>() {

											@Override
											public Boolean call() {
												Build build = buildManager.load(buildId);
												if (e instanceof ExplicitException) 
													build.setErrorMessage(e.getMessage());
												else 
													build.setErrorMessage(Throwables.getStackTraceAsString(e));
												buildManager.save(build);
												RetryCondition retryCondition = RetryCondition.parse(job, job.getRetryCondition());
												return retryCondition.matches(build);
											}
											
										})) {
											log(e, jobLogger);
											jobLogger.log("Job will be retried after a while...");
											transactionManager.run(new Runnable() {

												@Override
												public void run() {
													Build build = buildManager.load(buildId);
													build.setErrorMessage(null);
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
												throw e2;
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
											throw e;
										}
									}
								} finally {
									jobContexts.remove(jobToken);
								}
							}
						} catch (Throwable e) {
							throw maskSecrets(e, jobSecretsToMask);
						} finally {
							try {
								transactionManager.run(new Runnable() {
	
									@Override
									public void run() {
										Build build = buildManager.load(buildId);
										Build.push(build);
										try {
											if (job.getArtifacts() != null) {
												jobLogger.log("Publishing job artifacts...");
												build.publishArtifacts(serverWorkspace, job.getArtifacts());
											}

											jobLogger.log("Processing job reports...");
											for (JobReport report: job.getReports())
												report.process(build, serverWorkspace, jobLogger);
										} finally {
											Build.pop();
										}
									}
									
								});
								FileUtils.deleteDir(serverWorkspace);
							} catch (Throwable e) {
								throw maskSecrets(e, jobSecretsToMask);
							} 
							jobLogger.log("Job finished");
						}
					}
					
				}), job.getTimeout() * 1000L));
				
				return executionRef.get();
			} else {
				throw new ExplicitException("No applicable job executor");
			}
		} finally {
			Build.pop();
		}
	}
	
	private void log(Throwable e, SimpleLogger logger) {
		if (e instanceof ExplicitException)
			logger.log(e.getMessage());
		else
			logger.log(e);
	}
	
	private RuntimeException maskSecrets(Throwable e, Collection<String> jobSecretsToMask) {
		if (e instanceof ExplicitException) {
			String errorMessage = e.getMessage();
			for (String secret: jobSecretsToMask)
				errorMessage = StringUtils.replace(errorMessage, secret, SecretInput.MASK);
			return new ExplicitException(errorMessage);
		} else {
			String stackTrace = Throwables.getStackTraceAsString(e);
			for (String secret: jobSecretsToMask)
				stackTrace = StringUtils.replace(stackTrace, secret, SecretInput.MASK);
			return new ExplicitException(stackTrace);
		}
	}
	
	@Override
	public JobContext getJobContext(String jobToken, boolean mustExist) {
		JobContext jobContext = jobContexts.get(jobToken);
		if (mustExist && jobContext == null)
			throw new ExplicitException("No job context found for specified job token");
		return jobContext;
	}
	
	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.FAILED, errorMessage);
		build.setFinishDate(new Date());
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware) {
			ObjectId commitId = ((CommitAware) event).getCommit().getCommitId();
			if (!commitId.equals(ObjectId.zeroId())) {
				ScriptIdentity.push(new JobIdentity(event.getProject(), commitId));
				try {
					BuildSpec buildSpec = event.getProject().getBuildSpec(commitId);
					if (buildSpec != null) {
						for (Job job: buildSpec.getJobs()) {
							JobTriggerMatch match = job.getTriggerMatch(event);
							if (match != null) {
								Map<String, List<List<String>>> paramMatrix = 
										ParamSupply.getParamMatrix(null, match.getTrigger().getParams());						
								Long projectId = event.getProject().getId();
								
								// run asynchrously as session may get closed due to exception
								transactionManager.runAfterCommit(new Runnable() {

									@Override
									public void run() {
										sessionManager.runAsync(new Runnable() {
											
											@Override
											public void run() {
												ThreadContext.bind(userManager.getSystem().asSubject());
												
												Project project = projectManager.load(projectId);
												try {
													new MatrixRunner<List<String>>(paramMatrix) {
														
														@Override
														public void run(Map<String, List<String>> paramMap) {
															submit(project, commitId, job.getName(), paramMap, match.getReason()); 
														}
														
													}.run();
												} catch (Throwable e) {
													String message = String.format("Error submitting build (project: %s, commit: %s, job: %s)", 
															project.getName(), commitId.name(), job.getName());
													logger.error(message, e);
												}
											}
											
										});
									}
									
								});
							}
						}
					}
				} catch (Throwable e) {
					String message = String.format("Error checking job triggers (project: %s, commit: %s)", 
							event.getProject().getName(), commitId.name());
					logger.error(message, e);
				} finally {
					ScriptIdentity.pop();
				}
			}
		}
	}
	
	@Transactional
	@Override
	public void resubmit(Build build, Map<String, List<String>> paramMap, String resubmitReason) {
		if (build.isFinished()) {
        	validate(build.getProject(), build.getCommitId());
			
			build.setStatus(Build.Status.WAITING);
			build.setFinishDate(null);
			build.setPendingDate(null);
			build.setRetryDate(null);
			build.setRunningDate(null);
			build.setSubmitDate(new Date());
			build.setSubmitter(SecurityUtils.getUser());
			build.setSubmitReason(resubmitReason);
			build.setCanceller(null);
			build.setCancellerName(null);
			
			buildParamManager.deleteParams(build);
			for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) {
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
			listenerRegistry.post(new BuildSubmitted(build));
		} else {
			throw new ExplicitException("Build #" + build.getNumber() + " not finished yet");
		}
	}

	@Sessional
	@Override
	public void cancel(Build build) {
		Long buildId = build.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						synchronized (DefaultJobManager.this) {
							transactionManager.run(new Runnable() {

								@Override
								public void run() {
									JobExecution execution = jobExecutions.get(buildId);
									if (execution != null) {
										execution.cancel(User.idOf(SecurityUtils.getUser()));
									} else {
										Build build = buildManager.load(buildId);
										if (!build.isFinished()) {
											build.setStatus(Status.CANCELLED);
											build.setFinishDate(new Date());
											build.setCanceller(SecurityUtils.getUser());
											listenerRegistry.post(new BuildFinished(build));
										}
									}
								}
								
							});
						}
					}
					
				});
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Key.JOB_EXECUTORS)
				jobExecutors = (List<JobExecutor>) setting.getValue();
		}
	}
	
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		jobExecutors = settingManager.getJobExecutors();
		thread = new Thread(this);
		thread.start();	
		
		for (Project project: projectManager.query())
			schedule(project);
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = ((Project) event.getEntity()).getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					Collection<String> tasksOfProject = scheduledTasks.remove(projectId);
					if (tasksOfProject != null) 
						tasksOfProject.stream().forEach(it->taskScheduler.unschedule(it));
				}
				
			});
		}
	}

	@Sessional
	@Listen
	public void on(RefUpdated event) {
		String branch = GitUtils.ref2branch(event.getRefName());
		Project project = event.getProject();
		if (branch != null && branch.equals(project.getDefaultBranch()) && !event.getNewCommitId().equals(ObjectId.zeroId()))
			schedule(project);
	}
	
	@Transactional
	@Listen
	public void on(ProjectCreated event) {
		Long projectId = event.getProject().getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				sessionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						Project project = projectManager.load(projectId);
						schedule(project);
					}
					
				});
			}
			
		});
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
					BuildSpec buildSpec = project.getBuildSpec(commitId);
					if (buildSpec != null) {
						ScheduledTimeReaches event = new ScheduledTimeReaches(project);
						for (Job job: buildSpec.getJobs()) {
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
				}
			}
		} catch (Exception e) {
			logger.error("Error scheduling project '" + project.getName() + "'", e);
		} finally {
			tasksOfProject = scheduledTasks.put(project.getId(), tasksOfProject);
			if (tasksOfProject != null)
				tasksOfProject.stream().forEach(it->taskScheduler.unschedule(it));
		}
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
						ThreadContext.bind(userManager.getSystem().asSubject());
						
						Project project = projectManager.load(projectId);
						new MatrixRunner<List<String>>(ParamSupply.getParamMatrix(null, trigger.getParams())) {
							
							@Override
							public void run(Map<String, List<String>> paramMap) {
								Build build = submit(project, commitId, job.getName(), paramMap, reason); 
								if (build.isFinished())
									resubmit(build, paramMap, "Resubmitted by schedule");
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
		if (thread != null) {
			Thread copy = thread;
			thread = null;
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
			try {
				synchronized (this) {
					transactionManager.run(new Runnable() {
		
						@Override
						public void run() {
							for (Build build: buildManager.queryUnfinished()) {
								if (build.getStatus() == Build.Status.RUNNING || build.getStatus() == Build.Status.PENDING) {
									JobExecution execution = jobExecutions.get(build.getId());
									if (execution != null) {
										if (execution.isTimedout())
											execution.cancel(null);
									} else if (thread != null) {
										try {
											jobExecutions.put(build.getId(), execute(build));
										} catch (Throwable t) {
											if (t instanceof ExplicitException)
												markBuildError(build, t.getMessage());
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
							for (Iterator<Map.Entry<Long, JobExecution>> it = jobExecutions.entrySet().iterator(); it.hasNext();) {
								Map.Entry<Long, JobExecution> entry = it.next();
								Build build = buildManager.get(entry.getKey());
								JobExecution execution = entry.getValue();
								if (build == null || build.isFinished()) {
									it.remove();
									execution.cancel(null);
								} else if (execution.isDone()) {
									it.remove();
									try {
										execution.check();
										build.setStatus(Build.Status.SUCCESSFUL);
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
										if (e.getCause() instanceof ExplicitException)
											build.setStatus(Build.Status.FAILED, e.getCause().getMessage());
										else
											build.setStatus(Build.Status.FAILED, e.getMessage());
									} catch (InterruptedException e) {
									} finally {
										build.setFinishDate(new Date());
										listenerRegistry.post(new BuildFinished(build));
									}
								}
							}
						}
						
					});
				}
				Thread.sleep(CHECK_INTERVAL);
			} catch (Throwable e) {
				logger.error("Error checking unfinished builds", e);
			} 
		}	
	}
	
	@Listen
	public void on(BuildSubmitted event) {
		Build build = event.getBuild();
		FileUtils.deleteDir(build.getPublishDir());
	}

	@Transactional
	@Listen
	public void on(BuildFinished event) {
		Build build = event.getBuild();
		for (BuildParam param: build.getParams()) {
			if (param.getType().equals(ParamSpec.SECRET)) 
				param.setValue(null);
		}

		Long buildId = build.getId();

		OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

			@Override
			public void run() {
				OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

					@Override
					public void run() {
						Build build = OneDev.getInstance(BuildManager.class).load(buildId);
						Build.push(build);
						try {
							Job job = (Job) VariableInterpolator.installInterceptor(build.getJob());
							for (PostBuildAction action: job.getPostBuildActions()) {
								if (ActionCondition.parse(job, action.getCondition()).matches(build))
									action.execute(build);
							}
						} catch (Throwable e) {
							String message = String.format("Error processing post build actions (project: %s, commit: %s, job: %s)", 
									build.getProject().getName(), build.getCommitHash(), build.getJobName());
							logger.error(message, e);
						} finally {
							Build.pop();
						}
					}
				});
			}
			
		});
	}
	
	@Override
	public boolean canPullCode(HttpServletRequest request, Project project) {
		String jobToken = Job.getToken(request);
		if (jobToken != null) {
			JobContext context = getJobContext(jobToken, false);					
			if (context != null)
				return context.getProjectName().equals(project.getName());
		}
		return false;
	}

	@Override
	public Map<CacheInstance, String> allocateJobCaches(String jobToken, Date currentTime, 
			Map<CacheInstance, Date> cacheInstances) {
		synchronized (jobContexts) {
			JobContext jobContext = getJobContext(jobToken, true);
			
			List<CacheInstance> sortedInstances = new ArrayList<>(cacheInstances.keySet());
			sortedInstances.sort(new Comparator<CacheInstance>() {
	
				@Override
				public int compare(CacheInstance o1, CacheInstance o2) {
					return cacheInstances.get(o2).compareTo(cacheInstances.get(o1));
				}
				
			});
		
			Collection<String> allAllocated = new HashSet<>();
			for (JobContext each: jobContexts.values())
				allAllocated.addAll(each.getAllocatedCaches());
			Map<CacheInstance, String> allocations = new HashMap<>();
			for (CacheSpec cacheSpec: jobContext.getCacheSpecs()) {
				Optional<CacheInstance> result = sortedInstances
						.stream()
						.filter(it->it.getCacheKey().equals(cacheSpec.getKey()))
						.filter(it->!allAllocated.contains(it.getName()))
						.findFirst();
				CacheInstance allocation;
				if (result.isPresent()) 
					allocation = result.get();
				else
					allocation = new CacheInstance(UUID.randomUUID().toString(), cacheSpec.getKey());
				allocations.put(allocation, cacheSpec.getPath());
				jobContext.getAllocatedCaches().add(allocation.getName());
				allAllocated.add(allocation.getName());
			}
			
			Consumer<CacheInstance> deletionMarker = new Consumer<CacheInstance>() {
	
				@Override
				public void accept(CacheInstance instance) {
					long ellapsed = currentTime.getTime() - cacheInstances.get(instance).getTime();
					if (ellapsed > jobContext.getCacheTTL() * 24L * 3600L * 1000L) {
						allocations.put(instance, null);
						jobContext.getAllocatedCaches().add(instance.getName());
						allAllocated.add(instance.getName());
					}
				}
				
			};
			
			cacheInstances.keySet()
					.stream()
					.filter(it->!allAllocated.contains(it.getName()))
					.forEach(deletionMarker);
			
			updateCacheCounts(jobContext, cacheInstances.keySet(), allAllocated);
			
			return allocations;
		}
	}
	
	private void updateCacheCounts(JobContext jobContext, Collection<CacheInstance> cacheInstances, 
			Collection<String> allAllocated) {
		for (CacheInstance cacheInstance: cacheInstances) {
			if (!allAllocated.contains(cacheInstance.getName())) {
				String cacheKey = cacheInstance.getCacheKey();
				Integer cacheCount = jobContext.getCacheCounts().get(cacheKey);
				if (cacheCount == null)
					cacheCount = 0;
				cacheCount++;
				jobContext.getCacheCounts().put(cacheKey, cacheCount);
			}
		}
	}

	@Override
	public void reportJobCaches(String jobToken, Collection<CacheInstance> cacheInstances) {
		synchronized (jobContexts) {
			JobContext jobContext = getJobContext(jobToken, true);
			Collection<String> allAllocated = new HashSet<>();
			for (JobContext each: jobContexts.values()) {
				if (each != jobContext)
					allAllocated.addAll(each.getAllocatedCaches());
			}
			updateCacheCounts(jobContext, cacheInstances, allAllocated);
		}
	}

}