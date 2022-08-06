package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.IssueAuthorizationManager;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultIssueAuthorizationManager extends BaseEntityManager<IssueAuthorization> 
		implements IssueAuthorizationManager {

	@Inject
	public DefaultIssueAuthorizationManager(Dao dao) {
		super(dao);
	}
	
	@Override
	public List<IssueAuthorization> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}

	@Transactional
	@Override
	public void authorize(Issue issue, User user) {
		boolean authorized = false;
		for (IssueAuthorization authorization: issue.getAuthorizations()) {
			if (authorization.getUser().equals(user)) {
				authorized = true;
				break;
			}
		}
		if (!authorized) {
			IssueAuthorization authorization = new IssueAuthorization();
			authorization.setIssue(issue);
			authorization.setUser(user);
			issue.getAuthorizations().add(authorization);
			save(authorization);
		}
	}

	@Override
	public IssueAuthorization find(Issue issue, User user) {
		EntityCriteria<IssueAuthorization> criteria = EntityCriteria.of(IssueAuthorization.class);
		criteria.add(Restrictions.eq(IssueAuthorization.PROP_ISSUE, issue));
		criteria.add(Restrictions.eq(IssueAuthorization.PROP_USER, user));
		return find(criteria);
	}
	
}
