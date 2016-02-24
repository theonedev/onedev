package com.pmease.gitplex.core.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.DefaultDao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestActivity;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.entity.Review.Result;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.manager.CommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.manager.UserManager;

@Singleton
public class DefaultReviewManager extends DefaultDao implements ReviewManager {

	private final PullRequestManager pullRequestManager;
	
	private final CommentManager commentManager;

	private final UnitOfWork unitOfWork;
	
	private final Set<PullRequestListener> pullRequestListeners;
	
	@Inject
	public DefaultReviewManager(Provider<Session> sessionProvider, 
			PullRequestManager pullRequestManager, CommentManager commentManager, 
			UnitOfWork unitOfWork, Set<PullRequestListener> pullRequestListeners) {
		super(sessionProvider);
		
		this.pullRequestManager = pullRequestManager;
		this.commentManager = commentManager;
		this.unitOfWork = unitOfWork;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public Review findBy(User reviewer, PullRequestUpdate update) {
		return find(EntityCriteria.of(Review.class)
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
		
		persist(review);	
		
		PullRequestActivity activity = new PullRequestActivity();
		if (result == Review.Result.APPROVE)
			activity.setAction(PullRequestActivity.Action.APPROVE);
		else
			activity.setAction(PullRequestActivity.Action.DISAPPROVE);
		activity.setDate(new Date());
		activity.setRequest(request);
		activity.setUser(reviewer);
		persist(activity);
		
		if (comment != null) {
			Comment requestComment = new Comment();
			requestComment.setRequest(request);
			requestComment.setUser(reviewer);
			requestComment.setContent(comment);
			
			commentManager.save(requestComment, false);
		}
		
		for (PullRequestListener listener: pullRequestListeners)
			listener.onReviewed(review, comment);

		final Long requestId = request.getId();
		
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						pullRequestManager.check(load(PullRequest.class, requestId));
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
		remove(review);
		
		PullRequestActivity activity = new PullRequestActivity();
		activity.setAction(PullRequestActivity.Action.UNDO_REVIEW);
		activity.setDate(new Date());
		activity.setRequest(review.getUpdate().getRequest());
		activity.setUser(GitPlex.getInstance(UserManager.class).getCurrent());
		persist(activity);

		final Long requestId = review.getUpdate().getRequest().getId();
		afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						pullRequestManager.check(load(PullRequest.class, requestId));
					}
					
				});
			}
			
		});
	}
}
