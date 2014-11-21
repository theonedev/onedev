package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.VoteInvitation;

public interface VoteInvitationManager {
	
	VoteInvitation find(User reviewer, PullRequest request);
	
}
