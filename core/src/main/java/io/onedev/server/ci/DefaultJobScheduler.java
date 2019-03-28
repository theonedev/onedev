package io.onedev.server.ci;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ci.jobexecutor.JobExecutor;
import io.onedev.server.ci.jobexecutor.JobExecutorProvider;
import io.onedev.server.ci.jobparam.JobParam;
import io.onedev.server.ci.jobtrigger.JobTrigger;
import io.onedev.server.entitymanager.Build2Manager;
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
import io.onedev.server.model.Build2.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.MatrixRunner;

@Singleton
public class DefaultJobScheduler implements JobScheduler, Runnable {

	private static final int CHECK_INTERVAL = 10;
	
	private final Build2Manager buildManager;
	
	private final UserManager userManager;
	
	private final ListenerRegistry listenerRegistry;
	
	private final TransactionManager transactionManager;
	
	private final ExecutorService executorService;
	
	private volatile Thread thread;
	
	private volatile boolean stopping;
	
	@Inject
	public DefaultJobScheduler(Build2Manager buildManager, UserManager userManager, 
			ListenerRegistry listenerRegistry, TransactionManager transactionManager, 
			ExecutorService executorService) {
		this.buildManager = buildManager;
		this.userManager = userManager;
		this.listenerRegistry = listenerRegistry;
		this.transactionManager = transactionManager;
		this.executorService = executorService;
	}

	@Sessional
	@Override
	public void submit(Project project, String commitHash, String jobName, Map<String, String> paramMap) {
		String commitLock = "job-schedule: " + project.getId() + "-" + commitHash;
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				LockUtils.call(commitLock, new Callable<Void>() {

					@Override
					public Void call() {
						transactionManager.run(new Runnable() {

							@Override
							public void run() {
								submit(project, commitHash, jobName, paramMap, new ArrayList<>()); 
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
	
	private void submit(Project project, String commitHash, String jobName, 
			Map<String, String> paramMap, List<String> dependencyChain) {
		Build2 build = buildManager.find(project, commitHash, jobName, paramMap);
		if (build == null) {
			build = new Build2();
			build.setProject(project);
			build.setCommitHash(commitHash);
			build.setJobName(jobName);
			build.setSubmitDate(new Date());
			build.setStatus(Status.WAITING);
			build.setUser(userManager.getCurrent());
			
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
				return;
			}
			
			dependencyChain.add(jobName);
			
			CISpec ciSpec = project.getCISpec(ObjectId.fromString(commitHash));
			if (ciSpec == null) {
				markBuildError(build, "No CI spec");
				return;
			}
			
			Job job = ciSpec.getJobMap().get(jobName);
			if (job == null) {
				markBuildError(build, "Job not found");
				return;
			}

			for (Dependency dependency: job.getDependencies()) {
				new MatrixRunner(getParamMatrix(dependency.getParams())).run(new MatrixRunner.Runnable() {
					
					@Override
					public void run(Map<String, String> params) {
						submit(project, commitHash, dependency.getJobName(), params, new ArrayList<>(dependencyChain));
					}
					
				});
			}

			buildManager.create(build);
			listenerRegistry.post(new BuildSubmitted(build));
		}
	}

	private void run(Build2 build) {
		JobExecutor jobExecutor = getJobExecutor(build);
		if (jobExecutor != null) {
			String runInstanceId = jobExecutor.run(build);
			if (runInstanceId != null) {
				build.setRunInstanceId(runInstanceId);
				build.setStatus(Status.RUNNING);
				build.setRunningDate(new Date());
				buildManager.save(build);
				listenerRegistry.post(new BuildRunning(build));
			}
		} else {
			markBuildError(build, "No applicable job executor");
		}
	}
	
	private void markBuildError(Build2 build, String errorMessage) {
		build.setErrorMessage(errorMessage);
		build.setStatus(Status.FAILED);
		if (build.getPendingDate() == null)
			build.setPendingDate(new Date());
		if (build.getRunningDate() == null)
			build.setRunningDate(new Date());
		if (build.getFinishDate() == null)
			build.setFinishDate(new Date());
		buildManager.save(build);
		listenerRegistry.post(new BuildFinished(build));
	}
	
	@Nullable
	private JobExecutor getJobExecutor(Build2 build) {
		List<JobExecutorProvider> providers = new ArrayList<>(OneDev.getExtensions(JobExecutorProvider.class));
		providers.sort(Comparator.comparing(JobExecutorProvider::getPriority));
		
		for (JobExecutorProvider provider: providers) {
			JobExecutor jobExecutor = provider.getExecutor(build);
			if (jobExecutor != null)
				return jobExecutor;
		}

		return null;
	}

	@Sessional
	@Listen
	public void on(ProjectEvent event) {
		if (event instanceof CommitAware) {
			ObjectId commitId = ((CommitAware) event).getCommitId();
			CISpec ciSpec = event.getProject().getCISpec(commitId);
			if (ciSpec != null) {
				for (Job job: ciSpec.getJobs()) {
					JobTrigger trigger = job.getMatchedTrigger(event);
					if (trigger != null) {
						new MatrixRunner(getParamMatrix(trigger.getParams())).run(new MatrixRunner.Runnable() {
							
							@Override
							public void run(Map<String, String> params) {
								submit(event.getProject(), commitId.name(), job.getName(), params);
							}
							
						});
					}
				}
			}
		}
	}
	
	@Transactional
	@Override
	public void resubmit(Build2 build) {
		for (BuildDependence dependence: build.getDependents())
			resubmit(dependence.getDependent());

		if (build.getStatus() == Status.FAILED || build.getStatus() == Status.SUCCESSFUL) {
			build.setStatus(Status.WAITING);
			build.setErrorMessage(null);
			build.setFinishDate(null);
			build.setPendingDate(null);
			build.setRunInstanceId(null);
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
		if (build.getStatus() == Status.WAITING || build.getStatus() == Status.PENDING) {
			markBuildError(build, "Build is cancelled");
		} else if (build.getStatus() == Status.RUNNING) {
			JobExecutor jobExecutor = getJobExecutor(build);
			if (jobExecutor != null) {
				if (jobExecutor.isRunning(build))
					jobExecutor.stop(build);
				else
					markBuildError(build, "Aborted for unknown reason");
			} else { 
				markBuildError(build, "No applicable job executor");
			}
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		thread = new Thread(this);
		thread.start();		
	}
	
	@Listen
	public void on(SystemStopping event) {
		stopping = true;
		synchronized (this) {
			notify();
		}
		while (thread != null ) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public synchronized void run() {
		while (!stopping) {
			try {
				wait(CHECK_INTERVAL * 1000L);
			} catch (InterruptedException e) {
			}
			transactionManager.run(new Runnable() {

				@Override
				public void run() {
					for (Build2 build: buildManager.queryUnfinished()) {
						if (build.getStatus() == Status.PENDING) {
							DefaultJobScheduler.this.run(build);									
						} else if (build.getStatus() == Status.RUNNING) {
							CISpec ciSpec = build.getProject().getCISpec(ObjectId.fromString(build.getCommitHash()));
							if (ciSpec != null) {
								Job job = ciSpec.getJobMap().get(build.getJobName());
								if (job != null) {
									if (System.currentTimeMillis() - build.getRunningDate().getTime() > job.getTimeout() * 1000L) {
										JobExecutor jobExecutor = getJobExecutor(build);
										if (jobExecutor != null) {
											if (jobExecutor.isRunning(build))
												jobExecutor.stop(build);
											else
												markBuildError(build, "Aborted for unknown reason");
										} else {
											markBuildError(build, "No applicable job executor");
										}
									}
								} else {
									markBuildError(build, "Job not found");
								}
							} else {
								markBuildError(build, "No CI spec");
							} 
						} else if (build.getStatus() == Status.WAITING) {
							boolean hasFailed = false;
							boolean hasUnfinished = false;
							
							for (BuildDependence dependence: build.getDependencies()) {
								Build2 dependency = dependence.getDependency();
								
								switch (dependency.getStatus()) {
								case FAILED:
									hasFailed = true;
									break;
								case RUNNING:
								case PENDING:
								case WAITING:
									hasUnfinished = true;
									break;
								default:
								}
							}
							
							if (hasFailed) {
								build.setStatus(Status.FAILED);
								build.setPendingDate(new Date());
								build.setRunningDate(new Date());
								build.setFinishDate(new Date());
								listenerRegistry.post(new BuildFinished(build));
							} else if (!hasUnfinished) {
								build.setStatus(Status.PENDING);
								build.setPendingDate(new Date());
								listenerRegistry.post(new BuildPending(build));
							}
						}
					}
				}
				
			});
		}	
		thread = null;
	}
	
}
