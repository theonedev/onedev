package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class HasSourceBranch extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.getSource() != null)
			return CheckResult.ACCEPT;
		else
			return CheckResult.REJECT;
	}

}
