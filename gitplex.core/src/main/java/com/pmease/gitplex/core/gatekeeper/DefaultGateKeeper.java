package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable
public class DefaultGateKeeper extends AbstractGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return ignored();
	}

}
