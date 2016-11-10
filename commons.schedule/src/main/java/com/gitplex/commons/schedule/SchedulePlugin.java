package com.gitplex.commons.schedule;

import javax.inject.Inject;

import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.loader.AbstractPlugin;

public class SchedulePlugin extends AbstractPlugin {

	private final TaskScheduler taskScheduler;
	
	@Inject
	public SchedulePlugin(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public void start() {
		if (Bootstrap.command == null)
			taskScheduler.start();
	}

	@Override
	public void stop() {
		if (taskScheduler.isStarted())
			taskScheduler.shutdown();
	}

}
