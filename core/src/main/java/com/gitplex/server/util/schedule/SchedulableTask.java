package com.gitplex.server.util.schedule;

import org.quartz.ScheduleBuilder;

public interface SchedulableTask {
	void execute();
	
	ScheduleBuilder<?> getScheduleBuilder();
}
