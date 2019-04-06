package io.onedev.server.ci.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Dependency;
import io.onedev.server.ci.InvalidCISpecException;
import io.onedev.server.ci.job.log.JobLogger;
import io.onedev.server.ci.job.log.LogLevel;
import io.onedev.server.ci.job.log.LogManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.trigger.JobTrigger;
import io.onedev.server.entitymanager.Build2Manager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.CommitAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.build2.BuildFinished;
import io.onedev.server.event.build2.BuildPending;
import io.onedev.server.event.build2.BuildRunning;
import io.onedev.server.event.build2.BuildSubmitted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.exception.OneException;
import io.onedev.server.model.Build2;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.MatrixRunner;

@Singleton
public class DefaultJobScheduler implements JobScheduler, Runnable {

	private static final int CHECK_INTERVAL = 5;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobScheduler.class);
	
	private enum Status {RUNNING, STOPPING, STOPPED};
	
	private final ProjectManager projectManager;
	
	private final Build2Manager buildManager;
	
	private final UserManager userManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final LogManager logManager;
	
	private final SettingManager settingManager;
	
	private volatile Status status;
	
	@Inject
	public DefaultJobScheduler(ProjectManager projectManager, Build2Manager buildManager, 
			UserManager userManager, ListenerRegistry listenerRegistry, SettingManager settingManager,
			TransactionManager transactionManager, LogManager logManager) {
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.logManager = logManager;
	}

	@Sessional
	@Override
	public void submit(Project project, String commitHash, String jobName, Map<String, List<String>> paramMatrix) {
		String lockKey = "job-schedule: " + project.getId() + "-" + commitHash;
		Long projectId = project.getId();
		Long userId = User.idOf(userManager.getCurrent());
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
								User user = (userId != null? userManager.load(userId): null);
								new MatrixRunner(paramMatrix).run(new MatrixRunner.Runnable() {
									
									@Override
									public void run(Map<String, String> params) {
										submit(project, user, commitHash, jobName, params, new ArrayList<>()); 
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
			paramMatrix.put(param.getName(), param.getValueProvider().getValues());
		return paramMatrix;
	}
	
	private List<Build2> submit(Project project, @Nullable User user, String commitHash, String jobName, 
			Map<String, String> paramMap, List<String> dependencyChain) {
		List<Build2> builds = buildManager.query(project, commitHash, jobName, paramMap);
		if (builds.isEmpty()) {
			Build2 build = new Build2();
			build.setProject(project);
			build.setCommitHash(commitHash);
			build.setJobName(jobName);
			build.setSubmitDate(new Date());
			build.setStatus(Build2.Status.WAITING);
			build.setUser(user);
			
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

			for (Dependency dependency: job.getDependencies()) {
				new MatrixRunner(getParamMatrix(dependency.getParams())).run(new MatrixRunner.Runnable() {
					
					@Override
					public void run(Map<String, String> params) {
						List<Build2> dependencyBuilds = submit(project, null, commitHash, dependency.getJobName(), 
								params, new ArrayList<>(dependencyChain));
						for (Build2 dependencyBuild: dependencyBuilds) {
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

	private void run(Build2 build) {
		ObjectId commitId = ObjectId.fromString(build.getCommitHash());
		try {
			CISpec ciSpec = build.getProject().getCISpec(commitId);
			if (ciSpec != null) {
				Job job = ciSpec.getJobMap().get(build.getJobName());
				if (job != null) {
					JobExecutor executor = settingManager.getJobExecutor(build.getProject(), commitId, job.getName(), job.getEnvironment());
					if (executor != null) {
						Long projectId = build.getProject().getId();
						Long buildId = build.getId();
						LogLevel loggerLevel = job.getLogLevel();
						
						String jobInstance = executor.run(job.getEnvironment(), job.getCommands(), new JobCallback() {

							@Override
							public void jobFinished(JobResult result, String resultMessage) {
								transactionManager.call(new Callable<Void>() {

									@Override
									public Void call() throws Exception {
										Build2 build = buildManager.load(buildId); 
										switch (result) {
										case SUCCESSFUL:
											build.setStatus(Build2.Status.SUCCESSFUL, resultMessage);
											break;
										case FAILED:
											build.setStatus(Build2.Status.FAILED, resultMessage);
											break;
										case CANCELLED:
											build.setStatus(Build2.Status.CANCELLED, resultMessage);
											break;
										case IN_ERROR:
											build.setStatus(Build2.Status.IN_ERROR, resultMessage);
											break;
										default:
											throw new OneException("Unexpected job result: " + result);
										}
										buildManager.save(build);
										listenerRegistry.post(new BuildFinished(build));
										return null;
									}
									
								});
							}

							@Override
							public JobLogger getJobLogger() {
								return logManager.getLogger(projectId, buildId, loggerLevel);
							}
							
						});
						if (jobInstance != null) {
							build.setStatus(Build2.Status.RUNNING);
							build.setJobInstance(jobInstance);
							build.setRunningDate(new Date());
							buildManager.save(build);
							listenerRegistry.post(new BuildRunning(build));
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
	
	private void markBuildError(Build2 build, String errorMessage) {
		build.setStatus(Build2.Status.IN_ERROR, errorMessage);
		build.setFinishDate(new Date());
		buildManager.save(build);
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware) {
			ObjectId commitId = ((CommitAware) event).getCommitId();
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
	
	@Transactional
	@Override
	public void resubmit(Build2 build) {
		for (BuildDependence dependence: build.getDependents())
			resubmit(dependence.getDependent());

		if (build.isFinished()) {
			build.setStatus(Build2.Status.WAITING);
			build.setFinishDate(null);
			build.setPendingDate(null);
			build.setJobInstance(null);
			build.setRunningDate(null);
			build.setSubmitDate(new Date());
			build.setUser(userManager.getCurrent());
			buildManager.save(build);
			listenerRegistry.post(new BuildSubmitted(build));
		} else {
			throw new OneException("Build #" + build.getNumber() + " not finished yet");
		}
	}

	@Transactional
	@Override
	public void cancel(Build2 build) {
		if (build.getStatus() == Build2.Status.RUNNING) {
			JobExecutor executor = settingManager.getJobExecutor(build.getJobInstance());
			if (executor != null)
				executor.stop(build.getJobInstance());
			else 
				markBuildError(build, "Aborted for unknown reason");
		} else if (!build.isFinished()) {
			build.setStatus(Build2.Status.CANCELLED);
			build.setFinishDate(new Date());
			buildManager.save(build);
			listenerRegistry.post(new BuildFinished(build));
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		status = Status.RUNNING;
		new Thread(this).start();		
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (status == Status.RUNNING) {
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
				wait(CHECK_INTERVAL * 1000L);
			} catch (InterruptedException e) {
			}
			try {
				boolean hasRunnings = transactionManager.call(new Callable<Boolean>() {
	
					@Override
					public Boolean call() {
						boolean hasRunnings = false;
						for (Build2 build: buildManager.queryUnfinished()) {
							if (build.getStatus() == Build2.Status.PENDING) {
								if (status == Status.RUNNING)
									DefaultJobScheduler.this.run(build);									
							} else if (build.getStatus() == Build2.Status.RUNNING) {
								hasRunnings = true;
								JobExecutor executor = settingManager.getJobExecutor(build.getJobInstance());
								if (executor != null) {
									ObjectId commitId = ObjectId.fromString(build.getCommitHash());
									try {
										CISpec ciSpec = build.getProject().getCISpec(commitId);
										if (ciSpec != null) {
											Job job = ciSpec.getJobMap().get(build.getJobName());
											if (job != null) {
												if (System.currentTimeMillis() - build.getRunningDate().getTime() > job.getTimeout() * 1000L)
													executor.stop(build.getJobInstance());
											} else {
												markBuildError(build, "Job not found");
											}
										} else {
											markBuildError(build, "No CI spec");
										} 
									} catch (InvalidCISpecException e) {
										markBuildError(build, e.getMessage());
									}
								} else {
									markBuildError(build, "Stopped for unknown reason");
								}
							} else if (build.getStatus() == Build2.Status.WAITING) {
								boolean hasUnsuccessful = false;
								boolean hasUnfinished = false;
								
								for (BuildDependence dependence: build.getDependencies()) {
									Build2 dependency = dependence.getDependency();
									
									if (dependency.getStatus() == Build2.Status.SUCCESSFUL)
										continue;
									else if (dependency.isFinished())
										hasUnsuccessful = true;
									else
										hasUnfinished = true;
								}
								
								if (hasUnsuccessful) {
									markBuildError(build, "There are failed dependency jobs");
								} else if (!hasUnfinished) {
									build.setStatus(Build2.Status.PENDING);
									build.setPendingDate(new Date());
									listenerRegistry.post(new BuildPending(build));
								}
							}
						}
						return hasRunnings;
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
	
}
