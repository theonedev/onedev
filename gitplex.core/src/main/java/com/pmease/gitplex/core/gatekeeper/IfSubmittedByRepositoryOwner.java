package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=300, icon="fa-user", category=GateKeeper.CATEGROY_CHECK_SUBMITTER, description=
		"This gate keeper will be passed if the commit is submitted by owner of the repository.")
public class IfSubmittedByRepositoryOwner extends AbstractGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter(), request.getTargetDepot());
    }

    private CheckResult check(User user, Depot depot) {
		if (depot.getOwner().equals(user))
			return passed(Lists.newArrayList("Submitted by repository owner."));
		else
			return failed(Lists.newArrayList("Not submitted by repository owner."));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot);
	}

}
