package io.onedev.server.event.project;

import java.util.Date;

import io.onedev.server.model.Project;

public class ScheduledTimeReaches extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;

	public ScheduledTimeReaches(Project project) {
		super(null, new Date(), project);
	}

	@Override
	public String getActivity() {
		return "Scheduled time reaches ";
	}

}
