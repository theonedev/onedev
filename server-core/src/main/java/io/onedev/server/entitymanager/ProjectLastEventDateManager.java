package io.onedev.server.entitymanager;

import io.onedev.server.model.ProjectLastEventDate;
import io.onedev.server.persistence.dao.EntityManager;

public interface ProjectLastEventDateManager extends EntityManager<ProjectLastEventDate> {

	void create(ProjectLastEventDate lastEventDate);
	
}
