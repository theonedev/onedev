package io.onedev.server.entitymanager;

import io.onedev.server.model.ProjectLastActivityDate;
import io.onedev.server.persistence.dao.EntityManager;

public interface ProjectLastEventDateManager extends EntityManager<ProjectLastActivityDate> {

	void create(ProjectLastActivityDate lastEventDate);
	
}
