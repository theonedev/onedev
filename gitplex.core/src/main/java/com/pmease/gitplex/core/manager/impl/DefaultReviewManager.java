package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.shiro.util.ThreadContext;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.TransactionInterceptor;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Review.Result;
import com.pmease.gitplex.core.entity.component.PullRequestEvent;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;

@Singleton
public class DefaultReviewManager extends AbstractEntityManager<Review> implements ReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final AccountManager accountManager;
	
	private final PullRequestCommentManager commentManager;

	private final UnitOfWork unitOfWork;
	
	private final Provider<Set<PullRequestListener>> listenersProvider;
	
	@Inject
	public DefaultReviewManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestCommentManager commentManager, AccountManager accountManager, 
			UnitOfWork unitOfWork, Provider<Set<PullRequestListener>> listenersProvider) {
		super(dao);
		
		this.pullRequestManager = pullRequestManager;
		this.commentManager = commentManager;
		this.accountManager = accountManager;
		this.unitOfWork = unitOfWork;
		this.listenersProvider = listenersProvider;
	}

	@Sessional
	@Override
	public Review find(Account reviewer, PullRequestUpdate update) {
		return find(EntityCriteria.of(Review.class)
				.add(Restrictions.eq("reviewer", reviewer)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, Account reviewer, Result result, String comment) {
		reviewer.setReviewEffort(reviewer.getReviewEffort()+1);
		
		Review review = new Review();
		review.setResult(result);
		review.setUpdate(request.getLatestUpdate());
		review.setReviewer(reviewer);

		dao.persist(review);	
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(request);
			requestComment.setUser(reviewer);
			requestComment.setContent(comment);
			
			commentManager.save(requestComment);
		}
		
		if (result == Result.APPROVE)
			request.setLastEvent(PullRequestEvent.APPROVED);
		else
			request.setLastEvent(PullRequestEvent.DISAPPROVED);
		request.setLastEventDate(null);
		request.setLastEventUser(reviewer);
		
		pullRequestManager.save(request);
		
		if (TransactionInterceptor.isInitiating()) {
			for (PullRequestListener listener: listenersProvider.get())
				listener.onReviewRequest(review, comment);
		}

		Long requestId = request.getId();
		
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
	public List<Review> findAll(PullRequest request) {
		EntityCriteria<Review> criteria = EntityCriteria.of(Review.class);
		criteria.createCriteria("update").add(Restrictions.eq("request", request));
		criteria.addOrder(Order.asc("date"));
		return findAll(criteria);
	}

	@Transactional
	@Override
	public void delete(Review review) {
		dao.remove(review);
		
		Account user = accountManager.getCurrent();
		PullRequest request = review.getUpdate().getRequest();
		request.setLastEvent(PullRequestEvent.REVIEW_WITHDRAWED);
		request.setLastEventUser(user);
		request.setLastEventDate(new Date());
		pullRequestManager.save(request);
		
		if (TransactionInterceptor.isInitiating()) {
			for (PullRequestListener listener: listenersProvider.get()) {
				listener.onWithdrawReview(review, user);
			}
		}
		
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
