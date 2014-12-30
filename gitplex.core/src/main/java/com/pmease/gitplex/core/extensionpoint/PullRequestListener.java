package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestCommentReply;
import com.pmease.gitplex.core.model.Review;

@ExtensionPoint
public interface PullRequestListener {
	
	void onOpened(PullRequest request);
	
	void onUpdated(PullRequest request);
	
	void onCommented(PullRequestComment comment);
	
	void onCommentReplied(PullRequestCommentReply reply);

	void onReviewed(Review review);
	
	void onAssigned(PullRequest request);
	
	void onVerified(PullRequest request);
	
	void onIntegrated(PullRequest request);
	
	void onDiscarded(PullRequest request);

	void onIntegrationPreviewCalculated(PullRequest request);
	
}
