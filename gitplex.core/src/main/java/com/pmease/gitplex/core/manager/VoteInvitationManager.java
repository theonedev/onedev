package com.pmease.gitplex.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.manager.impl.DefaultVoteInvitationManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.VoteInvitation;

@ImplementedBy(DefaultVoteInvitationManager.class)
public interface VoteInvitationManager {
	
	VoteInvitation find(User reviewer, PullRequest request);
	
}
