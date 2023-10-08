package io.onedev.server.manager;

import io.onedev.server.model.BuildDependence;
import io.onedev.server.persistence.dao.EntityManager;

public interface BuildDependenceManager extends EntityManager<BuildDependence> {
	
	void create(BuildDependence dependence);
	
}
