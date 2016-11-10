package com.gitplex.commons.schedule;

import org.quartz.ScheduleBuilder;

public interface SchedulableTask {
	void execute();
	
	ScheduleBuilder<?> getScheduleBuilder();
}
