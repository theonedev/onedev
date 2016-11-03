package com.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;

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
			final ObjectId oldObjectId, final ObjectId newObjectId) {
		return aggregate(new Checker() {

			@Override
			public GateCheckResult check(GateKeeper gateKeeper) {
				return gateKeeper.checkPush(user, depot, refName, oldObjectId, newObjectId);
			}
			
		});
	}

}
