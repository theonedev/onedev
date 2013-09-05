package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class SubmittedViaPush extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.isAutoCreated())
			return CheckResult.ACCEPT;
		else
			return CheckResult.REJECT;
	}

}
