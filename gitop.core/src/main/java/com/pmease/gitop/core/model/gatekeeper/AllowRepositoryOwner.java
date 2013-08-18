package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class AllowRepositoryOwner extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		if (mergeRequest.getUser().getId().equals(mergeRequest.getTargetBranch().getRepository().getUser().getId()))
			return CheckResult.ACCEPT;
		else
			return CheckResult.REJECT;
	}

}
