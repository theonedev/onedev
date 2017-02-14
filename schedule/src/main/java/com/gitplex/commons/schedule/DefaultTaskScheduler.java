package com.gitplex.commons.schedule;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Singleton;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultTaskScheduler implements TaskScheduler {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultTaskScheduler.class);
	
	private Scheduler quartz;
	
	public DefaultTaskScheduler() {
		StdSchedulerFactory schedFactory = new StdSchedulerFactory();
		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.instanceName", "DefaultQuartzScheduler");
		props.setProperty("org.quartz.scheduler.rmi.export", "false");
		props.setProperty("org.quartz.scheduler.rmi.proxy", "false");
        props.setProperty("org.quartz.scheduler.wrapJobExecutionInUserTransaction", "false");
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "10");
        props.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		try {
			schedFactory.initialize(props);
			quartz = schedFactory.getScheduler();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isStarted() {
		try {
			return quartz.isStarted();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void start() {
		try {
			quartz.start();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized String schedule(SchedulableTask task) {
        try {
			JobDetail job = JobBuilder.newJob(HelperTask.class)
					.withIdentity(UUID.randomUUID().toString())
					.build();
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(UUID.randomUUID().toString())
					.withSchedule(task.getScheduleBuilder())
					.forJob(job)
					.build();
			trigger.getJobDataMap().put("task", task);
			quartz.scheduleJob(job, trigger);
			return job.getKey().getName();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized void unschedule(String taskId) {
		try {
			quartz.deleteJob(new JobKey(taskId));
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void shutdown() {
		try {
			quartz.shutdown();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

    public static class HelperTask implements Job {

    	@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			SchedulableTask task = (SchedulableTask) context.getTrigger().getJobDataMap().get("task");
			try {
				task.execute();
			} catch (Exception e) {
				logger.error("Error executing scheduled task", e);
			}
		}
		
	}

}
