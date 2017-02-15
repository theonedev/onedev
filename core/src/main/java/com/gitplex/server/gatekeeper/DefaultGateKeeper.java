package com.gitplex.server.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.util.editable.annotation.Editable;

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
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
		return ignored();
	}

}
