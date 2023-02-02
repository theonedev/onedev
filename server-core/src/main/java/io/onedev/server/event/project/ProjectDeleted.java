package io.onedev.server.event.project;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

import java.io.Serializable;
import java.util.Date;

public class ProjectDeleted implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final Long userId;
	
	private final Date date;
	
	public ProjectDeleted(User user, Date date, Project project) {
		userId = user.getId();
		this.date = date;
		projectId = project.getId();
	}
	
	public User getUser() {
		return OneDev.getInstance(UserManager.class).load(userId);
	}

	public Date getDate() {
		return date;
	}

	public Long getProjectId() {
		return projectId;
	}
	
}
