package com.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable
public class DefaultGateKeeper extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	public GateCheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return ignored();
	}

}
