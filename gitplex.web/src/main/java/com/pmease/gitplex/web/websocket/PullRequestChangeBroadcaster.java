package com.pmease.gitplex.web.websocket;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.request.component.IRequestablePage;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.listener.PullRequestListener;

@Singleton
public class PullRequestChangeBroadcaster implements PullRequestListener {
	
	private final Dao dao;
	
	@Inject
	public PullRequestChangeBroadcaster(Dao dao) {
		this.dao = dao;
	}
	
	@Override
	public void onOpenRequest(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onUpdateRequest(PullRequestUpdate update) {
		onChange(update.getRequest());
	}

	@Override
	public void onReviewRequest(Review review, String comment) {
		onChange(review.getUpdate().getRequest());
	}

	@Override
	public void onIntegrateRequest(PullRequest request, Account user, String comment) {
		onChange(request);
	}

	@Override
	public void onDiscardRequest(PullRequest request, Account user, String comment) {
		onChange(request);
	}

	@Override
	public void onIntegrationPreviewCalculated(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onCommentRequest(PullRequestComment comment) {
		onChange(comment.getRequest());
	}

	@Override
	public void onVerifyRequest(PullRequest request) {
		onChange(request);
	}

	@Override
	public void onAssignRequest(PullRequest request, Account user) {
		onChange(request);
	}

	private void onChange(PullRequest request) {
		/*
		 * Make sure that pull request and associated objects are committed before
		 * sending render request; otherwise rendering request may not reflect
		 * expected status as rendering happens in another thread which may get
		 * executed before pull request modification is committed.
		 */
		PullRequestChangeTrait trait = new PullRequestChangeTrait();
		trait.requestId = request.getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				// Send web socket message in a thread in order not to blocking UI operations
				IRequestablePage page = WicketUtils.getPage();
				GitPlex.getInstance(ExecutorService.class).execute(new Runnable() {

					@Override
					public void run() {
						WebSocketRenderBehavior.requestToRender(trait, page);
					}
					
				});
			}
			
		});
	}
	
	@Override
	public void onReopenRequest(PullRequest request, Account user, String comment) {
		onChange(request);
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

	@Override
	public void onRestoreSourceBranch(PullRequest request) {
	}

	@Override
	public void onDeleteSourceBranch(PullRequest request) {
	}

	@Override
	public void onWithdrawReview(Review review, Account user) {
	}

}