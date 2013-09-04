package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class NeverAccept extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		return CheckResult.REJECT;
	}

}
