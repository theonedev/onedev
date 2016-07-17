package com.pmease.gitplex.core.manager.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestActivity;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.component.PullRequestEvent;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.PullRequestActivityManager;

@Singleton
public class DefaultPullRequestActivityManager 
		extends AbstractEntityManager<PullRequestActivity> implements PullRequestActivityManager, PullRequestListener {

	@Inject
	public DefaultPullRequestActivityManager(Dao dao) {
		super(dao);
	}

	@Override
	public void onOpenRequest(PullRequest request) {
	}

	@Override
	public void onDeleteRequest(PullRequest request) {
	}

	@Transactional
	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setEvent(PullRequestEvent.REOPENED);
		activity.setUser(user);
		
		save(activity);
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
	}

	@Override
	public void onMentionAccount(PullRequest request, Account account) {
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account account) {
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
	}

	@Transactional
	@Override
	public void onReviewRequest(Review review, String comment) {
		PullRequestActivity activity = new PullRequestActivity();
		if (review.getResult() == Review.Result.APPROVE)
			activity.setEvent(PullRequestEvent.APPROVED);
		else
			activity.setEvent(PullRequestEvent.DISAPPROVED);
		activity.setDate(new Date());
		activity.setRequest(review.getUpdate().getRequest());
		activity.setUser(review.getReviewer());
		save(activity);
	}

	@Transactional
	@Override
	public void onAssignRequest(PullRequest request, Account user) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setEvent(PullRequestEvent.ASSIGNED);
		activity.setDate(new Date());
		activity.setRequest(request);
		activity.setUser(user);
		save(activity);
	}

	@Override
	public void onVerifyRequest(PullRequest request) {
	}

	@Transactional
	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setEvent(PullRequestEvent.INTEGRATED);
		activity.setUser(user);
		
		save(activity);
	}

	@Transactional
	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setDate(new Date());
		activity.setEvent(PullRequestEvent.DISCARDED);
		activity.setUser(user);

		save(activity);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
	}

	@Override
	public void onInvitingReview(ReviewInvitation invitation) {
	}

	@Override
	public void pendingIntegration(PullRequest request) {
	}

	@Override
	public void pendingUpdate(PullRequest request) {
	}

	@Override
	public void pendingApproval(PullRequest request) {
	}

	@Transactional
	@Override
	public void onRestoreSourceBranch(PullRequest request) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setEvent(PullRequestEvent.SOURCE_BRANCH_RESTORED);
		activity.setDate(new Date());
		activity.setUser(GitPlex.getInstance(AccountManager.class).getCurrent());
		save(activity);
	}

	@Transactional
	@Override
	public void onDeleteSourceBranch(PullRequest request) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setRequest(request);
		activity.setEvent(PullRequestEvent.SOURCE_BRANCH_DELETED);
		activity.setDate(new Date());
		activity.setUser(GitPlex.getInstance(AccountManager.class).getCurrent());
		save(activity);
	}

	@Transactional
	@Override
	public void onWithdrawReview(Review review, Account user) {
		PullRequestActivity activity = new PullRequestActivity();
		activity.setEvent(PullRequestEvent.REVIEW_WITHDRAWED);
		activity.setDate(new Date());
		activity.setRequest(review.getUpdate().getRequest());
		activity.setUser(user);
		save(activity);
	}

}
