package io.onedev.server.ci.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;

import org.eclipse.jgit.lib.ObjectId;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneException;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Dependency;
import io.onedev.server.ci.InvalidCISpecException;
import io.onedev.server.ci.job.log.LogManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.BuildCommitsAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildQueueing;
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
import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.model.support.jobexecutor.SourceSnapshot;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultJobScheduler implements JobScheduler, Runnable, SchedulableTask {

	private static final int CHECK_INTERVAL = 1000; // check internal in milli-seconds
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobScheduler.class);
	
	private enum Status {STARTED, STOPPING, STOPPED};
	
	private final Map<Long, JobExecution> jobExecutions = new ConcurrentHashMap<>();
	
	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	private final UserManager userManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final SessionManager sessionManager;
	
	private final LogManager logManager;
	
	private final SettingManager settingManager;
	
	private final ExecutorService executorService;
	
	private final Set<DependencyPopulator> dependencyPopulators;
	
	private final TaskScheduler taskScheduler;
	
	private volatile List<JobExecutor> jobExecutors;
	
	private String taskId;
	
	private volatile Status status;
	
	@Inject
	public DefaultJobScheduler(ProjectManager projectManager, BuildManager buildManager, 
			UserManager userManager, ListenerRegistry listenerRegistry, SettingManager settingManager,
			TransactionManager transactionManager, LogManager logManager, ExecutorService executorService,
			SessionManager sessionManager, Set<DependencyPopulator> dependencyPopulators, 
			TaskScheduler taskScheduler) {
		this.projectManager = projectManager;
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
	}

	@Sessional
	@Override
	public void submit(Project project, String commitHash, String jobName, Map<String, List<String>> paramMatrix) {
		String lockKey = "job-schedule: " + project.getId() + "-" + commitHash;
		Long projectId = project.getId();
		Long submitterId = User.idOf(userManager.getCurrent());
		transactionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				LockUtils.call(lockKey, new Callable<Void>() {

					@Override
					public Void call() {
						transactionManager.run(new Runnable() {

							@Override
							public void run() {
								Project project = projectManager.load(projectId);
								User submitter = (submitterId != null? userManager.load(submitterId): null);
								new MatrixRunner(paramMatrix).run(new MatrixRunner.Runnable() {
									
									@Override
									public void run(Map<String, String> params) {
										submit(project, submitter, commitHash, jobName, params, new ArrayList<>()); 
									}
									
								});
							}
							
						});
						return null;
					}
					
				});
			}
			
		});
	}
	
	private Map<String, List<String>> getParamMatrix(List<JobParam> params) {
		Map<String, List<String>> paramMatrix = new LinkedHashMap<>();
		for (JobParam param: params) 
			paramMatrix.put(param.getName(), param.getValuesProvider().getValues());
		return paramMatrix;
	}
	
	private List<Build> submit(Project project, @Nullable User user, String commitHash, String jobName, 
			Map<String, String> paramMap, List<String> dependencyChain) {
		List<Build> builds = buildManager.query(project, commitHash, jobName, paramMap);
		if (builds.isEmpty()) {
			Build build = new Build();
			build.setProject(project);
			build.setCommitHash(commitHash);
			build.setJobName(jobName);
			build.setSubmitDate(new Date());
			build.setStatus(Build.Status.WAITING);
			build.setSubmitter(user);
			
			builds.add(build);
			
			for (Map.Entry<String, String> entry: paramMap.entrySet()) {
				BuildParam param = new BuildParam();
				param.setBuild(build);
				param.setName(entry.getKey());
				param.setValue(entry.getValue());
				build.getParams().add(param);
			}
			
			if (dependencyChain.contains(jobName)) {
				dependencyChain.add(jobName);
				markBuildError(build, "Circular job dependencies found: " + dependencyChain);
				return builds;
			}
			
			dependencyChain.add(jobName);
			
			CISpec ciSpec;
			try {
				ciSpec = project.getCISpec(ObjectId.fromString(commitHash));
				if (ciSpec == null) {
					markBuildError(build, "No CI spec");
					return builds;
				}
			} catch (InvalidCISpecException e) {
				markBuildError(build, e.getMessage());
				return builds;
			}
			
			Job job = ciSpec.getJobMap().get(jobName);
			if (job == null) {
				markBuildError(build, "Job not found");
				return builds;
			}

			try {
				JobParam.validateParamMap(job.getParamSpecs(), paramMap);
			} catch (ValidationException e) {
				markBuildError(build, e.getMessage());
				return builds;
			}
			
			for (Dependency dependency: job.getDependencies()) {
				new MatrixRunner(getParamMatrix(dependency.getJobParams())).run(new MatrixRunner.Runnable() {
					
					@Override
					public void run(Map<String, String> params) {
						List<Build> dependencyBuilds = submit(project, null, commitHash, dependency.getJobName(), 
								params, new ArrayList<>(dependencyChain));
						for (Build dependencyBuild: dependencyBuilds) {
							BuildDependence dependence = new BuildDependence();
							dependence.setDependency(dependencyBuild);
							dependence.setDependent(build);
							build.getDependencies().add(dependence);
						}
					}
					
				});
			}

			buildManager.create(build);
			listenerRegistry.post(new BuildSubmitted(build));
		}
		return builds;
	}
	
	@Nullable
	private JobExecutor getJobExecutor(Project project, ObjectId commitId, String jobName, String image) {
		for (JobExecutor executor: jobExecutors) {
			if (executor.isApplicable(project, commitId, jobName, image))
				return executor;
		}
		return null;
	}

	private void run(Build build) {
		ObjectId commitId = ObjectId.fromString(build.getCommitHash());
		try {
			CISpec ciSpec = build.getProject().getCISpec(commitId);
			if (ciSpec != null) {
				Job job = ciSpec.getJobMap().get(build.getJobName());
				if (job != null) {
					JobExecutor executor = getJobExecutor(build.getProject(), commitId, job.getName(), job.getEnvironment());
					if (executor != null) {
						if (executor.hasCapacity()) {
							build.setStatus(Build.Status.RUNNING);
							build.setRunningDate(new Date());
							buildManager.save(build);
							listenerRegistry.post(new BuildRunning(build));
							
							SourceSnapshot snapshot;
							if (job.isCloneSource()) 
								snapshot = new SourceSnapshot(build.getProject(), commitId);
							else 
								snapshot = null;
							
							Logger logger = logManager.getLogger(build, job.getLogLevel()); 
							
							Long buildId = build.getId();
							JobExecution execution = new JobExecution(executorService.submit(new Runnable() {

								@Override
								public void run() {
									logger.info("Creating workspace...");
									File workspace = FileUtils.createTempDir("workspace");
									try {
										Map<String, String> envVars = new HashMap<>();
										Set<String> includeFiles = new HashSet<>();
										Set<String> excludeFiles = new HashSet<>();
										
										sessionManager.run(new Runnable() {

											@Override
											public void run() {
												Build build = buildManager.load(buildId);
												logger.info("Populating dependencies...");
												for (BuildDependence dependence: build.getDependencies()) {
													for (DependencyPopulator populator: dependencyPopulators)
														populator.populate(dependence.getDependency(), workspace, logger);
												}
												envVars.put("ONEDEV_PROJECT", build.getProject().getName());
												envVars.put("ONEDEV_COMMIT", commitId.name());
												envVars.put("ONEDEV_JOB", job.getName());
												envVars.put("ONEDEV_BUILD_NUMBER", String.valueOf(build.getNumber()));
												for (BuildParam param: build.getParams()) 
													envVars.put("ONEDEV_PARAM_" + param.getName().toUpperCase(), param.getValue());
												
												for (JobOutcome outcome: job.getOutcomes()) {
													PatternSet patternSet = PatternSet.fromString(outcome.getFilePatterns());
													includeFiles.addAll(patternSet.getIncludes());
													excludeFiles.addAll(patternSet.getExcludes());
												}
											}
											
										});

										logger.info("Executing job with executor '" + executor.getName() + "'...");
										
										List<String> commands = Splitter.on("\n").trimResults().omitEmptyStrings()
												.splitToList(job.getCommands());
										executor.execute(job.getEnvironment(), workspace, envVars, commands, snapshot, 
												job.getCaches(), new PatternSet(includeFiles, excludeFiles), logger);
										
										sessionManager.run(new Runnable() {

											@Override
											public void run() {
												logger.info("Collecting job outcomes...");
												Build build = buildManager.load(buildId);
												for (JobOutcome outcome: job.getOutcomes())
													outcome.process(build, workspace, logger);
											}
											
										});
									} catch (Exception e) {
										if (ExceptionUtils.find(e, InterruptedException.class) == null)
											logger.error("Error running build", e);
										throw e;
									} finally {
										logger.info("Deleting workspace...");
										executor.cleanDir(workspace);
										FileUtils.deleteDir(workspace);
										logger.info("Workspace deleted");
									}
								}
								
							}), job.getTimeout() * 1000L);
							
							JobExecution prevExecution = jobExecutions.put(build.getId(), execution);
							
							if (prevExecution != null)
								prevExecution.cancel(null);
						}
					} else {
						markBuildError(build, "No applicable job executor");
					}
				} else {
					markBuildError(build, "Job not found");
				}
			} else {
				markBuildError(build, "No CI spec");
			}
		} catch (InvalidCISpecException e) {
			markBuildError(build, e.getMessage());
		}
	}
	
	private void markBuildError(Build build, String errorMessage) {
		build.setStatus(Build.Status.IN_ERROR, errorMessage);
		build.setFinishDate(new Date());
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof BuildCommitsAware) {
			for (ObjectId commitId: ((BuildCommitsAware) event).getBuildCommits()) {
				try {
					CISpec ciSpec = event.getProject().getCISpec(commitId);
					if (ciSpec != null) {
						for (Job job: ciSpec.getJobs()) {
							JobTrigger trigger = job.getMatchedTrigger(event);
							if (trigger != null)
								submit(event.getProject(), commitId.name(), job.getName(), getParamMatrix(trigger.getParams()));
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
	public void resubmit(Build build) {
		for (BuildDependence dependence: build.getDependents())
			resubmit(dependence.getDependent());

		if (build.isFinished()) {
			build.setStatus(Build.Status.WAITING);
			build.setFinishDate(null);
			build.setQueueingDate(null);
			build.setRunningDate(null);
			build.setSubmitDate(new Date());
			build.setSubmitter(userManager.getCurrent());
			buildManager.save(build);
			listenerRegistry.post(new BuildSubmitted(build));
		} else {
			throw new OneException("Build #" + build.getNumber() + " not finished yet");
		}
	}

	@Sessional
	@Override
	public void cancel(Build build) {
		JobExecution execution = jobExecutions.get(build.getId());
		if (execution != null)
			execution.cancel(User.getCurrentId());
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
							if (build.getStatus() == Build.Status.QUEUEING) {
								if (status == Status.STARTED) 
									run(build);									
							} else if (build.getStatus() == Build.Status.RUNNING) {
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
									build.setStatus(Build.Status.QUEUEING);
									build.setQueueingDate(new Date());
									listenerRegistry.post(new BuildQueueing(build));
								}
							}
						}
						for (Iterator<Map.Entry<Long, JobExecution>> it = jobExecutions.entrySet().iterator(); it.hasNext();) {
							Map.Entry<Long, JobExecution> entry = it.next();
							Build build = buildManager.get(entry.getKey());
							JobExecution execution = entry.getValue();
							if (build == null || build.getStatus() != Build.Status.RUNNING) {
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
								} catch (Exception e) {
									build.setStatus(Build.Status.FAILED, e.getMessage());
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
	
	@Override
	public void execute() {
		for (JobExecutor executor: jobExecutors)
			executor.checkCaches();
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}
	
}
