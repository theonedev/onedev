package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

public class ApprovedByRepositoryOwner extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		User repositoryOwner = request.getDestination().getRepository().getOwner();
		
		return repositoryOwner.checkApprovalSince(request.getBaseUpdate());
	}

}
