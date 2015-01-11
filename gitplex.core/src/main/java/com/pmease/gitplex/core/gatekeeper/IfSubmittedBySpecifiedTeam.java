package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="pa-group-o", category=GateKeeper.CATEGROY_CHECK_SUBMITTER, description=
		"This gate keeper will be passed if the commit is submitted by a member of specified team.")
public class IfSubmittedBySpecifiedTeam extends TeamAwareGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter());
    }

	private CheckResult check(User user) {
		for (Membership membership: user.getMemberships()) {
			if (membership.getTeam().equals(getTeam()))
				return passed(Lists.newArrayList("Submitted by a member of team '" + getTeam().getName() + "'."));
		}
		return failed(Lists.newArrayList("Not submitted by a member of team '" + getTeam().getName() + "'."));
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return check(user);
	}

}
