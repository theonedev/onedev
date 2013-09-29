package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
@Editable
public class ApprovedByProjectOwner extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		User projectOwner = request.getDestination().getProject().getOwner();
		
		Vote.Result result = projectOwner.checkVoteSince(request.getBaseUpdate());
		
		if (result == null) {
			request.inviteToVote(Sets.newHashSet(projectOwner), 1);
			return pending("To be approved by user '" + projectOwner.getName() + "'.");
		} else if (result.isAccept()) {
			return accept("Approved by user '" + projectOwner.getName() + "'.");
		} else {
			return reject("Rejected by user '" + projectOwner.getName() + "'.");
		}  
	}

}
