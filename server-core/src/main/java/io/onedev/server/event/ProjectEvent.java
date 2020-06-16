package io.onedev.server.event;

import java.util.Date;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.LastUpdate;

public abstract class ProjectEvent extends Event {

	private final Project project;
	
	public ProjectEvent(User user, Date date, Project project) {
		super(user, date);
		this.project = project;
	}

	public Project getProject() {
		return project;
	}
	
	public abstract String getActivity(boolean withEntity);
	
	public LastUpdate getLastUpdate() {
		LastUpdate lastUpdate = new LastUpdate();
		lastUpdate.setUser(getUser());
		lastUpdate.setActivity(getActivity(false));
		lastUpdate.setDate(getDate());
		return lastUpdate;
	}

}
