package com.gitplex.commons.schedule;

public interface TaskScheduler {
	
	String schedule(SchedulableTask task);
	
	void unschedule(String taskId);

	void start();
	
	void shutdown();
	
	boolean isStarted();
}
