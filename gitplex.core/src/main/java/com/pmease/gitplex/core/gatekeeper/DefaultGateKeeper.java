package com.pmease.gitplex.core.gatekeeper;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable
public class DefaultGateKeeper extends AbstractGateKeeper {

	@Override
	protected GateKeeper trim(Depot depot) {
		return this;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return ignored();
	}

}
