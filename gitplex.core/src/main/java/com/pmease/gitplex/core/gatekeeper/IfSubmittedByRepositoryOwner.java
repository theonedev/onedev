package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

@Editable(order=700, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is submitted by owner of the repository.")
public class IfSubmittedByRepositoryOwner extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter(), request.getTargetDepot());
    }

    private CheckResult check(Account user, Depot depot) {
		if (depot.getOwner().equals(user))
			return passed(Lists.newArrayList("Submitted by repository owner."));
		else
			return failed(Lists.newArrayList("Not submitted by repository owner."));
    }
    
	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot);
	}

}
