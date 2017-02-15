package com.gitplex.server.util.schedule;

public interface TaskScheduler {
	
	String schedule(SchedulableTask task);
	
	void unschedule(String taskId);

	void start();
	
	void stop();
}
