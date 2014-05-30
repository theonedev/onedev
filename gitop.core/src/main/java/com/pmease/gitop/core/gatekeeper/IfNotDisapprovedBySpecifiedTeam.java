package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=600, icon="icon-group", description=
		"This gate keeper will be passed if the commit is not disapproved by specified team.")
public class IfNotDisapprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (Membership membership: getTeam().getMemberships()) {
			Vote.Result result = membership.getUser().checkVoteSince(request.getBaseUpdate());
			if (result == Vote.Result.DISAPPROVE) {
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
