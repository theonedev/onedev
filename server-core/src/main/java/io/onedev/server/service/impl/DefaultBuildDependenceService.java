package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.BuildDependence;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.BuildDependenceService;

@Singleton
public class DefaultBuildDependenceService extends BaseEntityService<BuildDependence> implements BuildDependenceService {

	@Transactional
	@Override
	public void create(BuildDependence dependence) {
		Preconditions.checkState(dependence.isNew());
		dao.persist(dependence);
	}
	
}
