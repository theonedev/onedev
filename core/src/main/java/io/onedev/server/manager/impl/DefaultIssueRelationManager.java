package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.IssueRelationManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueRelation;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueRelationManager extends AbstractEntityManager<IssueRelation>
		implements IssueRelationManager {

	@Inject
	public DefaultIssueRelationManager(Dao dao) {
		super(dao);
	}

	@Override
	public IssueRelation find(Issue current, Issue other) {
		EntityCriteria<IssueRelation> criteria = EntityCriteria.of(IssueRelation.class);
		criteria.add(Restrictions.eq("current", current));
		criteria.add(Restrictions.eq("other", other));
		return find(criteria);
	}

}
