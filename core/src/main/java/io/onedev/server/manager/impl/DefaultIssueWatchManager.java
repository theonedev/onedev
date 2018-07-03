package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.IssueWatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueWatchManager extends AbstractEntityManager<IssueWatch> 
		implements IssueWatchManager {

	@Inject
	public DefaultIssueWatchManager(Dao dao) {
		super(dao);
	}

	@Override
	public IssueWatch find(Issue issue, User user) {
		EntityCriteria<IssueWatch> criteria = newCriteria();
		criteria.add(Restrictions.eq("issue", issue));
		criteria.add(Restrictions.eq("user", user));
		return find(criteria);
	}
	
}
