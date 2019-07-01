package io.onedev.server.ci.job;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.MatrixRunner;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneException;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.InvalidCISpecException;
import io.onedev.server.ci.JobDependency;
import io.onedev.server.ci.job.log.JobLogManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.BuildCommitAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildPending;
import io.onedev.server.event.build.BuildRunning;
import io.onedev.server.event.build.BuildSubmitted;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.Setting;
import io.onedev.server.model.Setting.Key;
import io.onedev.server.model.User;
import io.onedev.server.model.support.JobContext;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.CodePullAuthorizationSource;
import io.onedev.server.util.Input;
import io.onedev.server.util.JobLogger;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.SecretInput;
import io.onedev.server.util.patternset.PatternSet;
import jersey.repackaged.com.google.common.collect.Sets;

@Singleton
public class DefaultJobManager implements JobManager, Runnable, SchedulableTask, CodePullAuthorizationSource {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobManager.class);
	
	private enum Status {STARTED, STOPPING, STOPPED};
	
	private final Map<String, JobContext> jobContexts = new ConcurrentHashMap<>();
	
	private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();
	
	private final BuildManager buildManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final JobLogManager logManager;
	
	private final UserManager userManager;
	
	private final SettingManager settingManager;
	
	private final ExecutorService executorService;
	
	private final Set<DependencyPopulator> dependencyPopulators;
	
	private final TaskScheduler taskScheduler;
	
	private final BuildParamManager buildParamManager;
	
	private volatile List<JobExecutor> jobExecutors;
	
	private String taskId;
	
	private volatile Status status;
	
	@Inject
	public DefaultJobManager(BuildManager buildManager, UserManager userManager,
			ListenerRegistry listenerRegistry, SettingManager settingManager,
			TransactionManager transactionManager, JobLogManager logManager, ExecutorService executorService,
			SessionManager sessionManager, Set<DependencyPopulator> dependencyPopulators, 
			TaskScheduler taskScheduler, BuildParamManager buildParamManager) {
		this.settingManager = settingManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.logManager = logManager;
		this.executorService = executorService;
		this.dependencyPopulators = dependencyPopulators;
		this.sessionManager = sessionManager;
		this.taskScheduler = taskScheduler;
		this.buildParamManager = buildParamManager;
	}

	@Transactional
	@Override
	public Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, @Nullable User submitter) {
    	Lock lock = LockUtils.getLock("job-schedule: " + project.getId() + "-" + commitId.name());
    	transactionManager.mustRunAfterTransaction(new Runnable() {

			@Override
			public void run() {
				lock.unlock();
			}
    		
    	});
    	
		// Lock to guarantee uniqueness of build (by project, commit, job and parameters)
    	try {
        	lock.lockInterruptibly();
			return submit(project, commitId, jobName, paramMap, submitter, new LinkedHashSet<>()); 
    	} catch (Exception e) {
    		throw ExceptionUtils.unchecked(e);
		}
	}
	
	private Build submit(Project project, ObjectId commitId, String jobName, 
			Map<String, List<String>> paramMap, @Nullable User submitter, 
			Set<String> checkedJobNames) {
		Build build = new Build();
		build.setProject(project);
		build.setCommitHash(commitId.name());
		build.setJobName(jobName);
		build.setSubmitDate(new Date());
		build.setStatus(Build.Status.WAITING);
		build.setSubmitter(submitter);
		
		JobParam.validateParamMap(build.getJob().getParamSpecMap(), paramMap);
		
		if (!checkedJobNames.add(jobName)) {
			String message = String.format("Circular job dependencies found (%s)", checkedJobNames);
			throw new OneException(message);
		}

		Map<String, List<String>> paramMapToQuery = new HashMap<>(paramMap);
		for (InputSpec paramSpec: build.getJob().getParamSpecs()) {
			if (paramSpec instanceof SecretInput)
				paramMapToQuery.remove(paramSpec.getName());
		}

		Collection<Build> builds = buildManager.query(project, commitId, jobName, paramMapToQuery);
		
		if (builds.isEmpty()) {
			for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) {
				InputSpec paramSpec = Preconditions.checkNotNull(build.getJob().getParamSpecMap().get(entry.getKey()));
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
			
			for (JobDependency dependency: build.getJob().getDependencies()) {
				new MatrixRunner<List<String>>(JobParam.getParamMatrix(dependency.getJobParams())) {
					
					@Override
					public void run(Map<String, List<String>> params) {
						Build dependencyBuild = submit(project, commitId, dependency.getJobName(), 
								params, submitter, new LinkedHashSet<>(checkedJobNames));
						BuildDependence dependence = new BuildDependence();
						dependence.setDependency(dependencyBuild);
						dependence.setDependent(build);
						build.getDependencies().add(dependence);
					}
					
				}.run();
			}

			buildManager.create(build);
			listenerRegistry.post(new BuildSubmitted(build));
			return build;
		} else {
			return builds.iterator().next();
		}
	}
	
	@Nullable
	private JobExecutor getJobExecutor(Project project, ObjectId commitId, String jobName, String image) {
		for (JobExecutor executor: jobExecutors) {
			if (executor.isApplicable(project, commitId, jobName, image))
				return executor;
		}
		return null;
	}

	private void execute(Build build) {
		try {
			String jobToken = UUID.randomUUID().toString();
			Collection<String> jobSecretsToMask = Sets.newHashSet(jobToken);
			Job job = build.getJob();
			ObjectId commitId = ObjectId.fromString(build.getCommitHash());
			JobExecutor executor = getJobExecutor(build.getProject(), commitId, job.getName(), job.getEnvironment());
			if (executor != null) {
				JobLogger logger = logManager.getLogger(build, jobSecretsToMask); 
				
				Long buildId = build.getId();
				String projectName = build.getProject().getName();
				File projectGitDir = build.getProject().getGitDir();
				JobExecution execution = new JobExecution(executorService.submit(new Runnable() {

					@Override
					public void run() {
						logger.log("Creating server workspace...");
						File serverWorkspace = FileUtils.createTempDir("server-workspace");
						try {
							Map<String, String> envVars = new HashMap<>();
							Set<String> includeFiles = new HashSet<>();
							Set<String> excludeFiles = new HashSet<>();
							
							sessionManager.run(new Runnable() {

								@Override
								public void run() {
									Build build = buildManager.load(buildId);
									logger.log("Populating job dependencies...");
									for (BuildDependence dependence: build.getDependencies()) {
										for (DependencyPopulator populator: dependencyPopulators)
											populator.populate(dependence.getDependency(), serverWorkspace, logger);
									}
									envVars.put("ONEDEV_PROJECT", build.getProject().getName());
									envVars.put("ONEDEV_COMMIT", commitId.name());
									envVars.put("ONEDEV_JOB", job.getName());
									envVars.put("ONEDEV_BUILD_NUMBER", String.valueOf(build.getNumber()));
									for (Entry<String, Input> entry: build.getParamInputs().entrySet()) {
										String paramName = entry.getKey();
										if (build.isParamVisible(paramName)) {
											String paramType = entry.getValue().getType();
											List<String> paramValues = entry.getValue().getValues();
											if (paramValues.size() > 1) {
												int index = 1;
												for (String value: paramValues) {
													if (paramType.equals(InputSpec.SECRET)) 
														value = build.getSecretValue(value);
													envVars.put(paramName + "_" + index++, value);
												}
											} else if (paramValues.size() == 1) {
												String value = paramValues.iterator().next();
												if (paramType.equals(InputSpec.SECRET)) 
													value = build.getSecretValue(value);
												envVars.put(paramName, value);
											}
										}
									}
									
									for (JobOutcome outcome: job.getOutcomes()) {
										PatternSet patternSet = PatternSet.fromString(outcome.getFilePatterns());
										includeFiles.addAll(patternSet.getIncludes());
										excludeFiles.addAll(patternSet.getExcludes());
									}
								}
								
							});

							logger.log("Executing job with executor '" + executor.getName() + "'...");
							
							List<String> commands = Splitter.on("\n").trimResults(CharMatcher.is('\r')).splitToList(job.getCommands());
							
							JobContext jobContext = new JobContext(projectName, projectGitDir, job.getEnvironment(), 
									serverWorkspace, envVars, commands, job.isCloneSource(), commitId, job.getCaches(), 
									new PatternSet(includeFiles, excludeFiles), logger) {

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

							};
							
							jobContexts.put(jobToken, jobContext);
							try {
								executor.execute(jobToken, jobContext);
							} finally {
								jobContexts.remove(jobToken);
							}
							
							sessionManager.run(new Runnable() {

								@Override
								public void run() {
									logger.log("Processing job outcomes...");
									Build build = buildManager.load(buildId);
									for (JobOutcome outcome: job.getOutcomes())
										outcome.process(build, serverWorkspace, logger);
								}
								
							});
						} catch (Exception e) {
							if (ExceptionUtils.find(e, InterruptedException.class) == null) {
								DefaultJobManager.logger.debug("Error running build", e);
								if (e.getMessage() != null)
									logger.log(e.getMessage());
							}
							String errorMessage = e.getMessage();
							if (errorMessage != null) {
								for (String secret: jobSecretsToMask)
									errorMessage = StringUtils.replace(errorMessage, secret, SecretInput.MASK);
								throw new RuntimeException(errorMessage);
							} else {
								throw e;
							}
						} finally {
							logger.log("Deleting server workspace...");
							executor.cleanDir(serverWorkspace);
							FileUtils.deleteDir(serverWorkspace);
							logger.log("Job finished");
						}
					}
					
				}), job.getTimeout() * 1000L);
				
				JobExecution prevExecution = jobExecutions.put(build.getId(), execution);
				
				if (prevExecution != null)
					prevExecution.cancel(null);
			} else {
				markBuildError(build, "No applicable job executor");
			}
		} catch (InvalidCISpecException e) {
			markBuildError(build, e.getMessage());
		}
	}
	
	@Override
	public JobContext getJobContext(String jobToken) {
		return jobContexts.get(jobToken);
	}
	
	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.IN_ERROR, errorMessage);
		build.setFinishDate(new Date());
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof BuildCommitAware) {
			ObjectId commitId = ((BuildCommitAware) event).getBuildCommit();
			if (!commitId.equals(ObjectId.zeroId())) {
				try {
					CISpec ciSpec = event.getProject().getCISpec(commitId);
					if (ciSpec != null) {
						for (Job job: ciSpec.getJobs()) {
							JobTrigger trigger = job.getMatchedTrigger(event);
							if (trigger != null) {
								new MatrixRunner<List<String>>(JobParam.getParamMatrix(trigger.getParams())) {
									
									@Override
									public void run(Map<String, List<String>> paramMap) {
										submit(event.getProject(), commitId, job.getName(), paramMap, null); 
									}
									
								}.run();
							}
						}
					}
				} catch (Exception e) {
					String message = String.format("Error checking job triggers (project: %s, commit: %s)", 
							event.getProject().getName(), commitId.name());
					logger.error(message, e);
				}
			}
		}
	}
	
	@Transactional
	@Override
	public void resubmit(Build build, Map<String, List<String>> paramMap, User submitter) {
		if (build.isFinished()) {
			build.setStatus(Build.Status.WAITING);
			build.setFinishDate(null);
			build.setPendingDate(null);
			build.setRunningDate(null);
			build.setSubmitDate(new Date());
			build.setSubmitter(submitter);
			buildParamManager.deleteParams(build);
			for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) {
				InputSpec paramSpec = build.getJob().getParamSpecMap().get(entry.getKey());
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
			throw new OneException("Build #" + build.getNumber() + " not finished yet");
		}
	}

	@Sessional
	@Override
	public void cancel(Build build, User canceller) {
		JobExecution execution = jobExecutions.get(build.getId());
		if (execution != null)
			execution.cancel(User.idOf(canceller));
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
	
	@Listen
	public void on(SystemStarted event) {
		status = Status.STARTED;
		jobExecutors = settingManager.getJobExecutors();
		new Thread(this).start();		
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
		if (status == Status.STARTED) {
			status = Status.STOPPING;
			while (status == Status.STOPPING) {
				synchronized (this) {
					notify();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	@Override
	public synchronized void run() {
		while (true) {
			try {
				wait(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
			try {
				boolean hasRunnings = transactionManager.call(new Callable<Boolean>() {
	
					@Override
					public Boolean call() {
						for (Build build: buildManager.queryUnfinished()) {
							if (build.getStatus() == Build.Status.PENDING || build.getStatus() == Build.Status.RUNNING) {
								JobExecution execution = jobExecutions.get(build.getId());
								if (execution != null) {
									if (execution.isTimedout())
										execution.cancel(null);
								} else {
									markBuildError(build, "Stopped for unknown reason");
								}
							} else if (build.getStatus() == Build.Status.WAITING) {
								boolean hasUnsuccessful = false;
								boolean hasUnfinished = false;
								
								for (BuildDependence dependence: build.getDependencies()) {
									Build dependency = dependence.getDependency();
									
									if (dependency.getStatus() == Build.Status.SUCCESSFUL)
										continue;
									else if (dependency.isFinished())
										hasUnsuccessful = true;
									else
										hasUnfinished = true;
								}
								
								if (hasUnsuccessful) {
									markBuildError(build, "There are failed dependency jobs");
								} else if (!hasUnfinished) {
									build.setStatus(Build.Status.PENDING);
									build.setPendingDate(new Date());
									listenerRegistry.post(new BuildPending(build));
									
									if (status == Status.STARTED) 
										execute(build);									
								}
							}
						}
						for (Iterator<Map.Entry<Long, JobExecution>> it = jobExecutions.entrySet().iterator(); it.hasNext();) {
							Map.Entry<Long, JobExecution> entry = it.next();
							Build build = buildManager.get(entry.getKey());
							JobExecution execution = entry.getValue();
							if (build == null || build.getStatus() != Build.Status.PENDING && build.getStatus() != Build.Status.RUNNING) {
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
									if (e.getCause() != null)
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
						return !jobExecutions.isEmpty();
					}
					
				});
				if (!hasRunnings && status == Status.STOPPING)
					break;
			} catch (Exception e) {
				logger.error("Error checking unfinished builds", e);
			}
		}	
		status = Status.STOPPED;
	}
	
	@Listen
	public void on(BuildFinished event) {
		for (BuildParam param: event.getBuild().getParams()) {
			if (param.getType().equals(InputSpec.SECRET)) 
				param.setValue(null);
		}
	}
	
	@Override
	public void execute() {
		for (JobExecutor executor: jobExecutors)
			executor.checkCaches();
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}

	@Override
	public boolean canPullCode(HttpServletRequest request, Project project) {
		String jobToken = request.getHeader(JOB_TOKEN_HTTP_HEADER);
		if (jobToken != null) {
			JobContext context = getJobContext(jobToken);					
			if (context != null)
				return context.getProjectName().equals(project.getName());
		}
		return false;
	}

}
