package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class IsFastForward extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.isFastForward())
			return CheckResult.ACCEPT;
		else
			return CheckResult.REJECT;
	}

}
