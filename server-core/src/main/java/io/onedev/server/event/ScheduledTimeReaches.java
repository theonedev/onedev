package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Project;

public class ScheduledTimeReaches extends ProjectEvent {
	
	public ScheduledTimeReaches(Project project) {
		super(null, new Date(), project);
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "Scheduled time reaches ";
		if (withEntity)
			activity += " in project " + getProject().getName();
		return activity;
	}
	
}
