package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(category=GateKeeper.CATEGORY_APPROVAL, order=600, icon="icon-group", description=
		"This condition will be satisfied if specified team did not reject the commit.")
public class NoRejectionBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(PullRequest request) {
		
		for (Membership membership: getTeam().getMemberships()) {
			Vote.Result result = membership.getUser().checkVoteSince(request.getBaseUpdate());
			if (result.isReject()) {
				return rejected("Rejected by user '" + membership.getUser().getName() + "'.");
			}
		}
		
		return accepted("Not rejected by anyone from team '" + getTeam().getName() + ".");
	}

}
