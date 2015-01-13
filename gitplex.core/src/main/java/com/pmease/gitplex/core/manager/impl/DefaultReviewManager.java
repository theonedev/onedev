package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestActivity;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.Review.Result;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultReviewManager implements ReviewManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestCommentManager pullRequestCommentManager;

	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultReviewManager(Dao dao, PullRequestManager pullRequestManager,
			PullRequestCommentManager pullRequestCommentManager,
			UnitOfWork unitOfWork, Set<PullRequestListener> pullRequestListeners) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestCommentManager = pullRequestCommentManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public Review findBy(User reviewer, PullRequestUpdate update) {
		return dao.find(EntityCriteria.of(Review.class)
				.add(Restrictions.eq("reviewer", reviewer)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, User reviewer, Result result, String comment) {
		reviewer.setReviewEffort(reviewer.getReviewEffort()+1);
		
		final Review review = new Review();
		review.setResult(result);
		review.setUpdate(request.getLatestUpdate());
		review.setReviewer(reviewer);
		
		dao.persist(review);	
		
		PullRequestActivity activity = new PullRequestActivity();
		if (result == Review.Result.APPROVE)
			activity.setAction(PullRequestActivity.Action.APPROVE);
		else
			activity.setAction(PullRequestActivity.Action.DISAPPROVE);
		activity.setDate(new Date());
		activity.setRequest(request);
		activity.setUser(reviewer);
		dao.persist(activity);
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(request);
			requestComment.setDate(activity.getDate());
			requestComment.setUser(reviewer);
			requestComment.setContent(comment);
			
			pullRequestCommentManager.save(requestComment, false);
		}
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onReviewed(review, comment);

		final Long requestId = request.getId();
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						pullRequestManager.check(dao.load(PullRequest.class, requestId));
					}
					
				});
			}
			
		});
	}

	@Sessional
	@Override
	public List<Review> findBy(PullRequest request) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return dao.query(criteria);
	}

	@Transactional
	@Override
	public void delete(Review review) {
		dao.remove(review);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setAction(PullRequestActivity.Action.UNDO_REVIEW);
		activity.setDate(new Date());
		activity.setRequest(review.getUpdate().getRequest());
		activity.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
		dao.persist(activity);

		final Long requestId = review.getUpdate().getRequest().getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						pullRequestManager.check(dao.load(PullRequest.class, requestId));
					}
					
				});
			}
			
		});
	}
}
