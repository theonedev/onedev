package com.pmease.gitplex.core.listener;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.CommentReply;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.ReviewInvitation;
import com.pmease.gitplex.core.entity.Account;

@ExtensionPoint
public interface PullRequestListener {
	
	void onOpened(PullRequest request);
	
	void onReopened(PullRequest request, @Nullable Account user, @Nullable String comment);
	
	void onUpdated(PullRequestUpdate update);
	
	void onMentioned(PullRequest request, Account user);
	
	void onMentioned(Comment comment, Account user);

	void onMentioned(CommentReply reply, Account user);
	
	void onCommented(Comment comment);
	
	void onCommentReplied(CommentReply reply);

	void onReviewed(Review review, @Nullable String comment);
	
	void onAssigned(PullRequest request);
	
	void onVerified(PullRequest request);
	
	void onIntegrated(PullRequest request, @Nullable Account user, @Nullable String comment);
	
	void onDiscarded(PullRequest request, @Nullable Account user, @Nullable String comment);

	void onIntegrationPreviewCalculated(PullRequest request);
	
	void onInvitingReview(ReviewInvitation invitation);
	
	void pendingIntegration(PullRequest request);
	
	void pendingUpdate(PullRequest request);
	
	void pendingApproval(PullRequest request);
	
}
