package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=600, icon="icon-group", description=
		"This gate keeper will be passed if the commit is not rejected by specified team.")
public class IfNotRejectedBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult doCheck(PullRequest request) {
		
		for (Membership membership: getTeam().getMemberships()) {
			Vote.Result result = membership.getUser().checkVoteSince(request.getBaseUpdate());
			if (result.isReject()) {
				return rejected("Rejected by user '" + membership.getUser().getName() + "'.");
			}
		}
		
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + ".");
	}

}
