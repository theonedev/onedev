package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=600, icon="fa-group", category=GateKeeper.CATEGROY_CHECK_REVIEW, description=
		"This gate keeper will be passed if the commit is not disapproved by specified team.")
public class IfNotDisapprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (Membership membership: getTeam().getMemberships()) {
			Review.Result result = membership.getUser().checkReviewSince(request.getReferentialUpdate());
			if (result == Review.Result.DISAPPROVE) {
				return failed(Lists.newArrayList("Disapproved by user '" + membership.getUser().getName() + "'."));
			}
		}
		
		return passed(Lists.newArrayList("Not disapproved by anyone from team '" + getTeam().getName() + "."));
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return passed(Lists.newArrayList("Not disapproved by anyone from team '" + getTeam().getName() + "'."));
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return passed(Lists.newArrayList("Not disapproved by anyone from team '" + getTeam().getName() + "'."));
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return passed(Lists.newArrayList("Not disapproved by anyone from team '" + getTeam().getName() + "'."));
	}

}
