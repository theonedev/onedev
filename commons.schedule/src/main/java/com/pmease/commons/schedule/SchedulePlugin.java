package com.pmease.commons.schedule;

import javax.inject.Inject;

import com.pmease.commons.loader.AbstractPlugin;

public class SchedulePlugin extends AbstractPlugin {

	private final TaskScheduler taskScheduler;
	
	@Inject
	public SchedulePlugin(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	
	@Override
	public void start() {
		taskScheduler.start();
	}

	@Override
	public void stop() {
		taskScheduler.shutdown();
	}

}
