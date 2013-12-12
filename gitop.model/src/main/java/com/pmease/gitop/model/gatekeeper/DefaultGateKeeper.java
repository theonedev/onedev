package com.pmease.gitop.model.gatekeeper;

import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
public class DefaultGateKeeper implements GateKeeper {

	@Override
	public Object trim(Object context) {
		return null;
	}

	@Override
	public CheckResult check(PullRequest request) {
		return new Accepted("Not defined.");
	}

}
