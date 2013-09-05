package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class AlwaysAccept extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		return CheckResult.ACCEPT;
	}

}
