package com.pmease.gitplex.core.event;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestComment;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Verification;

@ExtensionPoint
public interface PullRequestListener {
	
	void onOpenRequest(PullRequest request);
	
	void onReopenRequest(PullRequest request, @Nullable Account user);
	
	void onUpdateRequest(PullRequestUpdate update);
	
	void onReviewRequest(Review review);
	
	void onVerifyRequest(Verification verification);
	
	void onDeleteVerification(Verification verification);
	
	void onDeleteReview(Review review);
	
	void onMentionAccount(PullRequest request, Account account);
	
	void onMentionAccount(PullRequestComment comment, Account account);

	void onCommentRequest(PullRequestComment comment);
	
	void onAssignRequest(PullRequest request, Account user);
	
	void onRestoreSourceBranch(PullRequest request);
	
	void onDeleteSourceBranch(PullRequest request);
	
	void onIntegrateRequest(PullRequest request, @Nullable Account user);
	
	void onDiscardRequest(PullRequest request, @Nullable Account user);

	void onIntegrationPreviewCalculated(PullRequest request);
	
	void onInvitingReview(ReviewInvitation invitation);
	
	void pendingIntegration(PullRequest request);
	
	void pendingUpdate(PullRequest request);
	
	void pendingApproval(PullRequest request);
	
}
