package io.onedev.server.event.project;

import java.util.Date;

import io.onedev.server.model.Project;

public class ScheduledTimeReaches extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final String branch;

	public ScheduledTimeReaches(Project project, String branch) {
		super(null, new Date(), project);
		this.branch = branch;
	}

	public String getBranch() {
		return branch;
	}

	@Override
	public String getActivity() {
		return "Scheduled time reaches ";
	}

}
