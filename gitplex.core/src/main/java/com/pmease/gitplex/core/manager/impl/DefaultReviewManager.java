package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.ReviewManager;

@Singleton
public class DefaultReviewManager extends AbstractEntityManager<Review> implements ReviewManager {

	private final PullRequestCommentManager commentManager;
	
	private final AccountManager accountManager;

	@Inject
	public DefaultReviewManager(Dao dao, PullRequestCommentManager commentManager, AccountManager accountManager) {
		super(dao);
		
		this.commentManager = commentManager;
		this.accountManager = accountManager;
	}

	@Sessional
	@Override
	public Review find(Account user, PullRequestUpdate update) {
		return find(EntityCriteria.of(Review.class)
				.add(Restrictions.eq("user", user)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, Review.Result result, String comment) {
		Review review = new Review();
		review.setUpdate(request.getLatestUpdate());
		review.setResult(result);
		review.setUser(accountManager.getCurrent());
		review.setDate(new Date());
		save(review);	
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(review.getUpdate().getRequest());
			requestComment.setUser(review.getUser());
			requestComment.setContent(comment);
			
			commentManager.save(requestComment, false);
		}
	}

	@Sessional
	@Override
	public List<Review> findAll(PullRequest request) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return findAll(criteria);
	}

}
