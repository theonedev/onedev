package com.pmease.gitplex.core.listener;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;

@ExtensionPoint
public interface PullRequestListener {
	
	void onOpenRequest(PullRequest request);
	
	void onDeleteRequest(PullRequest request);
	
	void onReopenRequest(PullRequest request, @Nullable Account user, @Nullable String comment);
	
	void onUpdateRequest(PullRequestUpdate update);
	
	void onMentionAccount(PullRequest request, Account account);
	
	void onMentionAccount(PullRequestComment comment, Account account);

	void onCommentRequest(PullRequestComment comment);
	
	void onReviewRequest(Review review, @Nullable String comment);
	
	void onWithdrawReview(Review review, Account user);
	
	void onAssignRequest(PullRequest request, Account user);
	
	void onVerifyRequest(PullRequest request);
	
	void onRestoreSourceBranch(PullRequest request);
	
	void onDeleteSourceBranch(PullRequest request);
	
	void onIntegrateRequest(PullRequest request, @Nullable Account user, @Nullable String comment);
	
	void onDiscardRequest(PullRequest request, @Nullable Account user, @Nullable String comment);

	void onIntegrationPreviewCalculated(PullRequest request);
	
	void onInvitingReview(ReviewInvitation invitation);
	
	void pendingIntegration(PullRequest request);
	
	void pendingUpdate(PullRequest request);
	
	void pendingApproval(PullRequest request);
	
}
