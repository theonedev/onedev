package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.ReviewInvitation;

public interface NotificationManager {
	
	void notifyReview(ReviewInvitation invitation);
	
	void notifyIntegration(PullRequest request);
	
	void notifyUpdate(PullRequest request, boolean noMail);
	
	void pendingApproval(PullRequest request);
}