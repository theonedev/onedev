package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;

public abstract class CompositeGateKeeper extends AbstractGateKeeper {
	
	private static final long serialVersionUID = 1L;
	
	public static interface Checker {
		GateCheckResult check(GateKeeper gateKeeper);
	}
	
	protected abstract GateCheckResult aggregate(Checker checker);

	@Override
	public GateCheckResult doCheckRequest(final PullRequest request) {
		return aggregate(new Checker() {

			@Override
			public GateCheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkRequest(request);
			}
			
		});
	}

	@Override
	protected GateCheckResult doCheckFile(final Account user, final Depot depot, final String branch, final String file) {
		return aggregate(new Checker() {

			@Override
			public GateCheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkFile(user, depot, branch, file);
			}
			
		});
	}
	
	@Override
	protected GateCheckResult doCheckPush(final Account user, final Depot depot, final String refName, 
			final ObjectId oldCommit, final ObjectId newCommit) {
		return aggregate(new Checker() {

			@Override
			public GateCheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkPush(user, depot, refName, oldCommit, newCommit);
			}
			
		});
	}

}
