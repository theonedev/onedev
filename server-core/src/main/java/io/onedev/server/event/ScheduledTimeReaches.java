package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Project;

public class ScheduledTimeReaches extends ProjectEvent {
	
	public ScheduledTimeReaches(Project project) {
		super(null, new Date(), project);
	}

	@Override
	public String getActivity() {
		return "Scheduled time reaches ";
	}
	
}
