package io.onedev.server.util.schedule;

public interface TaskScheduler {
	
	void start();
	
	String schedule(SchedulableTask task);
	
	void unschedule(String taskId);

	void stop();
	
}
