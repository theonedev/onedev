package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.InheritableThreadLocalData;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior.PageId;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.listeners.PullRequestListener;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.CommentReply;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;

public class PullRequestChangeBroadcaster implements PullRequestListener {
	
	private final Dao dao;
	
	@Inject
	public PullRequestChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	@Override
	public void onOpened(PullRequest request) {
	}

	@Override
	public void onUpdated(PullRequestUpdate update) {
		onChange(update.getRequest());
	}

	@Override
	public void onReviewed(Review review, String comment) {
		onChange(review.getUpdate().getRequest());
	}

	@Override
	public void onIntegrated(PullRequest request, User user, String comment) {
		onChange(request);
	}

	@Override
	public void onDiscarded(PullRequest request, User user, String comment) {
		onChange(request);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onCommented(Comment comment) {
		onChange(comment.getRequest());
	}

	@Override
	public void onVerified(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onAssigned(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onCommentReplied(CommentReply reply) {
		onChange(reply.getComment().getRequest());
	}

	private void onChange(PullRequest request) {
		/*
		 * Make sure that pull request and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before pull request modification is committed.
		 */
		final PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = request.getId();
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
	public void onReopened(PullRequest request, User user, String comment) {
	}

	@Override
	public void onMentioned(PullRequest request, User user) {
	}

	@Override
	public void onMentioned(Comment comment, User user) {
	}

	@Override
	public void onMentioned(CommentReply reply, User user) {
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