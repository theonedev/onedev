package io.onedev.server.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.BranchWatchManager;
import io.onedev.server.model.BranchWatch;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

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
