package com.pmease.gitplex.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Review;

@SuppressWarnings("serial")
@Editable(order=600, icon="fa-group-o", description=
		"This gate keeper will be passed if the commit is not disapproved by specified team.")
public class IfNotDisapprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (Membership membership: getTeam().getMemberships()) {
			Review.Result result = membership.getUser().checkReviewSince(request.getReferentialUpdate());
			if (result == Review.Result.DISAPPROVE) {
				return disapproved("Disapproved by user '" + membership.getUser().getName() + "'.");
			}
		}
		
		return approved("Not disapproved by anyone from team '" + getTeam().getName() + ".");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return approved("Not disapproved by anyone from team '" + getTeam().getName() + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return approved("Not disapproved by anyone from team '" + getTeam().getName() + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return approved("Not disapproved by anyone from team '" + getTeam().getName() + "'.");
	}

}
