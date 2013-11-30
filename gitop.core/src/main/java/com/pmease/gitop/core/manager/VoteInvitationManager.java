package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultVoteInvitationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.VoteInvitation;

@ImplementedBy(DefaultVoteInvitationManager.class)
public interface VoteInvitationManager extends GenericDao<VoteInvitation> {
	
	VoteInvitation find(User reviewer, PullRequest request);
	
	/**
	 * Invite specified number of users in candidates to vote for this request.
	 * <p>
	 * 
	 * @param request
	 * 			pull request to invite users to vote
	 * @param candidates 
	 * 			a collection of users to invite users from
	 * @param count 
	 * 			number of users to invite
	 */
	void inviteToVote(PullRequest request, Collection<User> candidates, int count);

}
