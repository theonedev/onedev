package com.pmease.gitplex.core.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.extensionpoint.PullRequestListeners;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.ReviewManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestAudit;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestOperation;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.Review.Result;

@Singleton
public class DefaultReviewManager implements ReviewManager {

	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestListeners pullRequestListeners;
	
	@Inject
	public DefaultReviewManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestListeners pullRequestListeners) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestListeners = pullRequestListeners;
	}

	@Sessional
	@Override
	public Review find(User reviewer, PullRequestUpdate update) {
		return dao.find(EntityCriteria.of(Review.class)
				.add(Restrictions.eq("redviewer", reviewer)) 
				.add(Restrictions.eq("update", update)));
	}

	@Transactional
	@Override
	public void review(PullRequest request, User reviewer, Result result, String comment) {
		reviewer.setReviewEffort(reviewer.getReviewEffort()+1);
		
		Review review = new Review();
		review.setResult(result);
		review.setUpdate(request.getLatestUpdate());
		review.setReviewer(reviewer);
		
		review.getUpdate().getReviews().add(review);
		dao.persist(review);	
		
		PullRequestAudit audit = new PullRequestAudit();
		if (result == Review.Result.APPROVE)
			audit.setOperation(PullRequestOperation.APPROVE);
		else
			audit.setOperation(PullRequestOperation.DISAPPROVE);
		audit.setDate(new Date());
		audit.setRequest(request);
		audit.setUser(reviewer);
		dao.persist(audit);
		
		if (comment != null) {
			PullRequestComment requestComment = new PullRequestComment();
			requestComment.setRequest(request);
			requestComment.setDate(audit.getDate());
			requestComment.setUser(reviewer);
			requestComment.setContent(comment);
			dao.persist(requestComment);
		}
		
		

		pullRequestManager.onGateKeeperUpdate(request);
		
		final Long requestId = request.getId();
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				pullRequestListeners.asyncCall(requestId, new PullRequestListeners.Callback() {

					@Override
					protected void call(PullRequestListener listener, PullRequest request) {
						listener.onReviewed(request);
					}
					
				});
			}
			
		});
	}

}
