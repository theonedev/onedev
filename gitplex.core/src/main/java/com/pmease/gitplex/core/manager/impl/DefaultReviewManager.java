package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.util.ThreadContext;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestActivity;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Review.Result;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestActivityManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;

@Singleton
public class DefaultReviewManager extends AbstractEntityManager<Review> implements ReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final PullRequestActivityManager pullRequestActivityManager;
	
	private final AccountManager accountManager;
	
	private final PullRequestCommentManager commentManager;

	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultReviewManager(Dao dao, PullRequestActivityManager pullRequestActivityManager, 
			PullRequestManager pullRequestManager, PullRequestCommentManager commentManager, 
			AccountManager accountManager, UnitOfWork unitOfWork, 
			Set<PullRequestListener> pullRequestListeners) {
		super(dao);
		
		this.pullRequestActivityManager = pullRequestActivityManager;
		this.pullRequestManager = pullRequestManager;
		this.commentManager = commentManager;
		this.accountManager = accountManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public Review findBy(Account reviewer, PullRequestUpdate update) {
		return find(EntityCriteria.of(Review.class)
				.add(Restrictions.eq("reviewer", reviewer)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, Account reviewer, Result result, String comment) {
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
		pullRequestActivityManager.save(activity);
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(request);
			requestComment.setUser(reviewer);
			requestComment.setContent(comment);
			
			commentManager.save(requestComment);
		}
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onReviewRequest(review, comment);

		final Long requestId = request.getId();
		
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						try {
					        ThreadContext.bind(accountManager.getRoot().asSubject());
							pullRequestManager.check(dao.load(PullRequest.class, requestId));
						} finally {
							ThreadContext.unbindSubject();
						}
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
		return query(criteria);
	}

	@Transactional
	@Override
	public void delete(Review review) {
		dao.remove(review);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setAction(PullRequestActivity.Action.UNDO_REVIEW);
		activity.setDate(new Date());
		activity.setRequest(review.getUpdate().getRequest());
		activity.setUser(GitPlex.getInstance(AccountManager.class).getCurrent());
		pullRequestActivityManager.save(activity);

		Long requestId = review.getUpdate().getRequest().getId();
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						try {
					        ThreadContext.bind(accountManager.getRoot().asSubject());
							pullRequestManager.check(dao.load(PullRequest.class, requestId));
						} finally {
							ThreadContext.unbindSubject();
						}
					}
					
				});
			}
			
		});
	}
}
