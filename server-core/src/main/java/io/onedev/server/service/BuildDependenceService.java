package io.onedev.server.service;

import io.onedev.server.model.BuildDependence;

public interface BuildDependenceService extends EntityService<BuildDependence> {
	
	void create(BuildDependence dependence);
	
}
