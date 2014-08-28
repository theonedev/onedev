package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.manager.CommentVisitManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.CommentVisit;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultCommentVisitManager implements CommentVisitManager {

	private final Dao dao;
	
	private final UserManager userManager;
	
	@Inject
	public DefaultCommentVisitManager(Dao dao, UserManager userManager) {
		this.dao = dao;
		this.userManager = userManager;
	}

	@Override
	public List<CommentVisit> findByCommitDates(Repository repository, Date fromDate, Date toDate) {
		EntityCriteria<CommentVisit> criteria = EntityCriteria.of(CommentVisit.class);
		User user = userManager.getCurrent();
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("repository", repository));
		if (fromDate != null)
			criteria.add(Restrictions.ge("commitDate", fromDate));
		if (toDate != null)
			criteria.add(Restrictions.le("commitDate", toDate));
		criteria.addOrder(Order.asc("id"));
		return dao.query(criteria, 0, 0);
	}

	@Override
	public List<CommentVisit> findByCommitOrDates(Repository repository,
			String commit, Date fromDate, Date toDate) {
		EntityCriteria<CommentVisit> criteria = EntityCriteria.of(CommentVisit.class);
		User user = userManager.getCurrent();
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("repository", repository));
		Conjunction conjunction = Restrictions.conjunction();
		if (fromDate != null)
			conjunction.add(Restrictions.ge("commitDate", fromDate));
		if (toDate != null)
			conjunction.add(Restrictions.le("commitDate", toDate));
		criteria.add(Restrictions.or(Restrictions.eq("commit", commit), conjunction));
		criteria.addOrder(Order.asc("id"));
		return dao.query(criteria, 0, 0);
	}

	@Override
	public CommentVisit find(Repository repository, String commit, CommentPosition position) {
		EntityCriteria<CommentVisit> criteria = EntityCriteria.of(CommentVisit.class);
		User user = userManager.getCurrent();
		criteria.add(Restrictions.eq("user", user));
		criteria.add(Restrictions.eq("repository", repository));
		criteria.add(Restrictions.eq("commit", commit));
		criteria.add(Restrictions.eq("position", position));
		
		return dao.find(criteria);
	}

}
