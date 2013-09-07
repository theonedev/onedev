package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class IsFastForward extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.isFastForward())
			return accept("Is fast-forward.");
		else
			return reject("None fast-forward.");
	}

}
