package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=600, icon="icon-group", description=
		"This gate keeper will be passed if the commit is not rejected by specified team.")
public class IfNotRejectedBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		if (request.isNew())
			return accepted("Not rejected by anyone from team '" + getTeam().getName() + "'.");
		
		for (Membership membership: getTeam().getMemberships()) {
			Vote.Result result = membership.getUser().checkVoteSince(request.getBaseUpdate());
			if (result.isReject()) {
				return rejected("Rejected by user '" + membership.getUser().getName() + "'.");
			}
		}
		
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + ".");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + "'.");
	}

}
