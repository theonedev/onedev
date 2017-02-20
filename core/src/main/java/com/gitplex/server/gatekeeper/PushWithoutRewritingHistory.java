package com.gitplex.server.gatekeeper;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.ExceptionUtils;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=2000, icon="fa-ext fa-repo-lock", description="This gatekeeper will be passed if the push "
		+ "operation does not rewrite history of target branch. Rewriting history of public branches "
		+ "is dangerous, and it happens when user forces a push without merging/rebasing with the "
		+ "branch head")
public class PushWithoutRewritingHistory extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
		if (!oldObjectId.equals(ObjectId.zeroId()) && !newObjectId.equals(ObjectId.zeroId())) {
			try {
				if (GitUtils.isMergedInto(depot.getRepository(), oldObjectId, newObjectId)) {
					return passed(Lists.newArrayList("Not trying to rewrite history"));
				} else {
					return failed(Lists.newArrayList("Trying to rewrite history"));
				}
			} catch (Exception e) {
				if (ExceptionUtils.find(e, IncorrectObjectTypeException.class) != null)
					return ignored();
				else
					throw Throwables.propagate(e);
			}
		} else {
			return ignored();
		}
	}

}
