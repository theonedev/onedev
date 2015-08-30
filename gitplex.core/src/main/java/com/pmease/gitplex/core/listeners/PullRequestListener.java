package com.pmease.gitplex.core.listeners;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.CommentReply;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.ReviewInvitation;
import com.pmease.gitplex.core.model.User;

@ExtensionPoint
public interface PullRequestListener {
	
	void onOpened(PullRequest request);
	
	void onReopened(PullRequest request, @Nullable User user, @Nullable String comment);
	
	void onUpdated(PullRequestUpdate update);
	
	void onMentioned(PullRequest request, User user);
	
	void onMentioned(Comment comment, User user);

	void onMentioned(CommentReply reply, User user);
	
	void onCommented(Comment comment);
	
	void onCommentReplied(CommentReply reply);

	void onReviewed(Review review, @Nullable String comment);
	
	void onAssigned(PullRequest request);
	
	void onVerified(PullRequest request);
	
	void onIntegrated(PullRequest request, @Nullable User user, @Nullable String comment);
	
	void onDiscarded(PullRequest request, @Nullable User user, @Nullable String comment);

	void onIntegrationPreviewCalculated(PullRequest request);
	
	void onInvitingReview(ReviewInvitation invitation);
	
	void pendingIntegration(PullRequest request);
	
	void pendingUpdate(PullRequest request);
	
	void pendingApproval(PullRequest request);
	
}
