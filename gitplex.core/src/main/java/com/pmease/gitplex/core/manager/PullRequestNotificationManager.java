package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.extensionpoint.PullRequestListener;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.ReviewInvitation;

public interface PullRequestNotificationManager extends PullRequestListener {
	
	void notifyReview(ReviewInvitation invitation);
	
	void notifyIntegration(PullRequest request);
	
	void notifyUpdate(PullRequest request, boolean noMail);
	
	void pendingApproval(PullRequest request);
}