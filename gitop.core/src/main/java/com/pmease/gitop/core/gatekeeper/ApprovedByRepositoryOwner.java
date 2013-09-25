package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
@Editable
public class ApprovedByRepositoryOwner extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		User repositoryOwner = request.getDestination().getRepository().getOwner();
		
		Vote.Result result = repositoryOwner.checkVoteSince(request.getBaseUpdate());
		
		if (result == null) {
			request.inviteToVote(Sets.newHashSet(repositoryOwner), 1);
			return pending("To be approved by user '" + repositoryOwner.getName() + "'.");
		} else if (result.isAccept()) {
			return accept("Approved by user '" + repositoryOwner.getName() + "'.");
		} else {
			return reject("Rejected by user '" + repositoryOwner.getName() + "'.");
		}  
	}

}
