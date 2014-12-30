package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.ReviewInvitation;

public interface ReviewInvitationManager {
	
	ReviewInvitation find(User reviewer, PullRequest request);
	
	void save(ReviewInvitation reviewInvitation);
}
