package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;

public class ScheduledTimeReaches extends ProjectEvent {
	
	public ScheduledTimeReaches(Project project) {
		super(null, new Date(), project);
	}

	@Override
	public String getActivity() {
		return "Scheduled time reaches ";
	}

	@Override
	public ProjectEvent cloneIn(Dao dao) {
		return new ScheduledTimeReaches(dao.load(Project.class, getProject().getId()));
	}
	
}
