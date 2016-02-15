package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;

@SuppressWarnings("serial")
@Editable(order=50, icon="fa-group", category=GateKeeper.CATEGROY_CHECK_SUBMITTER, description=
		"This gate keeper will be passed if the commit is submitted by an user with "
		+ "write permission against the repository.")
public class IfSubmittedByRepositoryWriter extends AbstractGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return check(request.getSubmitter(), request.getTargetDepot());
	}
	
	private CheckResult check(User user, Depot depot) {
		if (user != null && user.asSubject().isPermitted(ObjectPermission.ofDepotPush(depot)))
			return passed(Lists.newArrayList("Submitted by repository writer"));
		else
			return failed(Lists.newArrayList("Not submitted by repository writer"));
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return check(user, depot);
	}

}
