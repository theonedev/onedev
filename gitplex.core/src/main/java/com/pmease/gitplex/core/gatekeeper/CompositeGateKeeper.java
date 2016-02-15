package com.pmease.gitplex.core.gatekeeper;

import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public abstract class CompositeGateKeeper extends AbstractGateKeeper {
	
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
	protected CheckResult doCheckCommit(final User user, final Depot depot, final String branch, final String commit) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkCommit(user, depot, branch, commit);
			}
			
		});
	}

	@Override
	protected CheckResult doCheckRef(final User user, final Depot depot, final String refName) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkRef(user, depot, refName);
			}
			
		});
	}

}
