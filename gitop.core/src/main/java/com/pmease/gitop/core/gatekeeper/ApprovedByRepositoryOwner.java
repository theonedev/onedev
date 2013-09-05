package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.util.EasySet;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

public class ApprovedByRepositoryOwner extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		User repositoryOwner = request.getDestination().getRepository().getOwner();
		
		CheckResult result = repositoryOwner.checkApprovalSince(request.getBaseUpdate());
		if (result.isPending())
			request.requestVote(EasySet.of(repositoryOwner));
		return result;
	}

}
