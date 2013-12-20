package com.pmease.commons.schedule;

import org.quartz.ScheduleBuilder;

public interface SchedulableTask {
	void execute();
	
	ScheduleBuilder<?> getScheduleBuilder();
}
