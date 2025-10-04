package io.onedev.server.service;

import io.onedev.server.model.ProjectLastActivityDate;

public interface ProjectLastEventDateService extends EntityService<ProjectLastActivityDate> {

	void create(ProjectLastActivityDate lastEventDate);
	
}
