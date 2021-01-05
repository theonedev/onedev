package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueWatchManager extends BaseEntityManager<IssueWatch> 
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

	@Override
	public void watch(Issue issue, User user, boolean watching) {
		IssueWatch watch = (IssueWatch) issue.getWatch(user, true);
		if (watch.isNew()) {
			watch.setWatching(watching);
			save(watch);
		}
	}
	
}
