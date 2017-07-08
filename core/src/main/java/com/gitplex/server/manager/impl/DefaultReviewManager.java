package com.gitplex.server.manager.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.manager.PullRequestStatusChangeManager;
import com.gitplex.server.manager.ReviewManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestStatusChange;
import com.gitplex.server.model.PullRequestStatusChange.Type;
import com.gitplex.server.model.Review;
import com.gitplex.server.model.User;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultReviewManager extends AbstractEntityManager<Review> implements ReviewManager {

	private final UserManager userManager;

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestStatusChangeManager pullRequestStatusChangeManager;
	
	@Inject
	public DefaultReviewManager(Dao dao, UserManager userManager, 
			PullRequestManager pullRequestManager, PullRequestStatusChangeManager pullRequestStatusChangeManager) {
		super(dao);
		
		this.userManager = userManager;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestStatusChangeManager = pullRequestStatusChangeManager;
	}

	@Transactional
	@Override
	public void save(Review review) {
		super.save(review);	

		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setNote(review.getNote());
		statusChange.setRequest(review.getRequest());
		if (review.isApproved()) {
			statusChange.setType(Type.APPROVED);
		} else {
			statusChange.setType(Type.DISAPPROVED);
		}
		statusChange.setUser(review.getUser());
		pullRequestStatusChangeManager.save(statusChange);
		
		review.getRequest().setLastEvent(statusChange);
		pullRequestManager.save(review.getRequest());
	}

	@Sessional
	@Override
	public List<Review> findAll(PullRequest request) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void delete(User user, PullRequest request) {
		for (Iterator<Review> it = request.getReviews().iterator(); it.hasNext();) {
			Review review = it.next();
			if (review.getUser().equals(user)) {
				delete(review);
				it.remove();
			}
		}
		
		PullRequestStatusChange statusChange = new PullRequestStatusChange();
		statusChange.setDate(new Date());
		statusChange.setRequest(request);
		statusChange.setType(Type.WITHDRAWED_REVIEW);
		statusChange.setNote("Review of user '" + user.getDisplayName() + "' is withdrawed");
		statusChange.setUser(userManager.getCurrent());
		pullRequestStatusChangeManager.save(statusChange);
		
		request.setLastEvent(statusChange);
		pullRequestManager.save(request);
	}

	@Override
	public Review find(PullRequest request, User user, String commit) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.add(Restrictions.eq("user", user))
				.add(Restrictions.eq("request", request))
				.add(Restrictions.eq("commit", commit));
		return find(criteria);
	}	
	
}
