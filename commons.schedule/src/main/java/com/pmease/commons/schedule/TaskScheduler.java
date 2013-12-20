package com.pmease.commons.schedule;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultTaskScheduler.class)
public interface TaskScheduler {
	
	String schedule(SchedulableTask task);
	
	void unschedule(String taskId);

	void start();
	
	void shutdown();
	
}
