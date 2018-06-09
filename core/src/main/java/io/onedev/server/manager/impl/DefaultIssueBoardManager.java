package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.IssueBoardManager;
import io.onedev.server.model.IssueBoard;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueBoardManager extends AbstractEntityManager<IssueBoard>
		implements IssueBoardManager {

	@Inject
	public DefaultIssueBoardManager(Dao dao) {
		super(dao);
	}

	@Override
	public IssueBoard find(Project project, String name) {
		EntityCriteria<IssueBoard> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("name", name));
		return find(criteria);
	}

}
