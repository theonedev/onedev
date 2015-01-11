package com.pmease.gitplex.core.gatekeeper;

import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
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
	protected CheckResult doCheckFile(final User user, final Branch branch, final String file) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkFile(user, branch, file);
			}
			
		});
	}
	
	@Override
	protected CheckResult doCheckCommit(final User user, final Branch branch, final String commit) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkCommit(user, branch, commit);
			}
			
		});
	}

	@Override
	protected CheckResult doCheckRef(final User user, final Repository repository, final String refName) {
		return aggregate(new Checker() {

			@Override
			public CheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkRef(user, repository, refName);
			}
			
		});
	}

}
