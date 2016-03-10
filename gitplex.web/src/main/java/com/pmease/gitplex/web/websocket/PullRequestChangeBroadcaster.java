package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.listener.PullRequestListener;
import com.pmease.gitplex.core.entity.Account;

public class PullRequestChangeBroadcaster implements PullRequestListener {
	
	private final Dao dao;
	
	@Inject
	public PullRequestChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	@Override
	public void onOpened(PullRequest request) {
		onChange(request, PullRequest.Event.OPENED);
	}

	@Override
	public void onUpdated(PullRequestUpdate update) {
		onChange(update.getRequest(), PullRequest.Event.UPDATED);
	}

	@Override
	public void onReviewed(Review review, String comment) {
		onChange(review.getUpdate().getRequest(), PullRequest.Event.REVIEWED);
	}

	@Override
	public void onIntegrated(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.INTEGRATED);
	}

	@Override
	public void onDiscarded(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.DISCARDED);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		onChange(request, PullRequest.Event.INTEGRATION_PREVIEW_CALCULATED);
	}

	@Override
	public void onCommented(Comment comment) {
		onChange(comment.getRequest(), PullRequest.Event.COMMENTED);
	}

	@Override
	public void onVerified(PullRequest request) {
		onChange(request, PullRequest.Event.VERIFIED);
	}

	@Override
	public void onAssigned(PullRequest request) {
		onChange(request, PullRequest.Event.ASSIGNED);
	}

	@Override
	public void onCommentReplied(CommentReply reply) {
		onChange(reply.getComment().getRequest(), PullRequest.Event.COMMENT_REPLIED);
	}

	private void onChange(PullRequest request, PullRequest.Event event) {
		/*
		 * Make sure that pull request and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before pull request modification is committed.
		 */
		final PullRequestChangeTrait trait = new PullRequestChangeTrait();
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
	public void onReopened(PullRequest request, Account user, String comment) {
		onChange(request, PullRequest.Event.REOPENED);
	}

	@Override
	public void onMentioned(PullRequest request, Account user) {
	}

	@Override
	public void onMentioned(Comment comment, Account user) {
	}

	@Override
	public void onMentioned(CommentReply reply, Account user) {
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

}