package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.listener.PullRequestListener;

public class PullRequestChangeBroadcaster implements PullRequestListener {
	
	private final Dao dao;
	
	@Inject
	public PullRequestChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	@Override
	public void onOpenRequest(PullRequest request) {
		onChange(request, PullRequest.Event.OPENED);
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		onChange(update.getRequest(), PullRequest.Event.UPDATED);
	}

	@Override
	public void onReviewRequest(Review review, String comment) {
		onChange(review.getUpdate().getRequest(), PullRequest.Event.REVIEWED);
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.INTEGRATED);
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.DISCARDED);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		onChange(request, PullRequest.Event.INTEGRATION_PREVIEW_CALCULATED);
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
		onChange(comment.getRequest(), PullRequest.Event.COMMENTED);
	}

	@Override
	public void onVerifyRequest(PullRequest request) {
		onChange(request, PullRequest.Event.VERIFIED);
	}

	@Override
	public void onAssignRequest(PullRequest request) {
		onChange(request, PullRequest.Event.ASSIGNED);
	}

	private void onChange(PullRequest request, PullRequest.Event event) {
		/*
		 * Make sure that pull request and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before pull request modification is committed.
		 */
		PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = request.getId();
		trait.requestEvent = event;
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				// Send web socket message in a thread in order not to blocking UI operations
				GitPlex.getInstance(ExecutorService.class).execute(new Runnable() {

					@Override
					public void run() {
						WebSocketRenderBehavior.requestToRender(trait, PageId.fromObj(InheritableThreadLocalData.get()));
					}
					
				});
			}
			
		});
	}
	
	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.REOPENED);
	}

	@Override
	public void onMentionAccount(PullRequest request, Account user) {
	}

	@Override
	public void onMentionAccount(PullRequestComment comment, Account user) {
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

	@Override
	public void onDeleteRequest(PullRequest request) {
	}

}