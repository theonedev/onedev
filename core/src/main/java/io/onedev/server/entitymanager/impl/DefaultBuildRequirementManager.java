package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import io.onedev.server.entitymanager.BuildRequirementManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.BuildRequirement;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildRequirementManager extends AbstractEntityManager<BuildRequirement> 
		implements BuildRequirementManager {

	@Inject
	public DefaultBuildRequirementManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void saveBuildRequirements(PullRequest request) {
		Collection<Long> ids = new HashSet<>();
		for (BuildRequirement requirement: request.getBuildRequirements()) {
			save(requirement);
			ids.add(requirement.getId());
		}
		if (!ids.isEmpty()) {
			Query query = getSession().createQuery("delete from BuildRequirement where request=:request and id not in (:ids)");
			query.setParameter("request", request);
			query.setParameter("ids", ids);
			query.executeUpdate();
		} else {
			Query query = getSession().createQuery("delete from BuildRequirement where request=:request");
			query.setParameter("request", request);
			query.executeUpdate();
		}
	}

}
