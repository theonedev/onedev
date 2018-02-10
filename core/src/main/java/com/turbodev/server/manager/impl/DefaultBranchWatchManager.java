package com.turbodev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.turbodev.server.manager.BranchWatchManager;
import com.turbodev.server.model.BranchWatch;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.dao.AbstractEntityManager;
import com.turbodev.server.persistence.dao.Dao;
import com.turbodev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBranchWatchManager extends AbstractEntityManager<BranchWatch> implements BranchWatchManager {

	@Inject
	public DefaultBranchWatchManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public Collection<BranchWatch> find(User user, Project project) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("project", project));
		return findAll(criteria);
	}

	@Override
	public Collection<BranchWatch> find(Project project, String branch) {
		EntityCriteria<BranchWatch> criteria = EntityCriteria.of(BranchWatch.class);
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("branch", branch));
		return findAll(criteria);
	}

}
