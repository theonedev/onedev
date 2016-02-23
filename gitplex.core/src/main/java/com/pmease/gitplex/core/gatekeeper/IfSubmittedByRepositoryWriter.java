package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;

@Editable(order=800, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is submitted by an user with "
		+ "write permission against the repository.")
public class IfSubmittedByRepositoryWriter extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
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
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot);
	}

}
