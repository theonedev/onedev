package com.pmease.gitop.model.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable
public class DefaultGateKeeper extends AbstractGateKeeper {

	@Override
	protected GateKeeper trim(Project project) {
		return this;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return accepted("Not defined.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return accepted("Not defined.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return accepted("Not defined.");
	}

}
