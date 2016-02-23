package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

public abstract class CompositeGateKeeper extends AbstractGateKeeper {
	
	private static final long serialVersionUID = 1L;
	
	public static interface Checker {
		CheckResult check(GateKeeper gateKeeper);
	}
	
	protected abstract CheckResult aggregate(Checker checker);

	@Override
	public CheckResult doCheckRequest(final PullRequest request) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkRequest(request);
			}
			
		});
	}

	@Override
	protected CheckResult doCheckFile(final User user, final Depot depot, final String branch, final String file) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkFile(user, depot, branch, file);
			}
			
		});
	}
	
	@Override
	protected CheckResult doCheckPush(final User user, final Depot depot, final String refName, 
			final ObjectId oldCommit, final ObjectId newCommit) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkPush(user, depot, refName, oldCommit, newCommit);
			}
			
		});
	}

}
