package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.TeamMembership;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
@Editable
public class NoRejectionBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		
		for (TeamMembership membership: getTeam().getMemberships()) {
			Vote.Result result = membership.getUser().checkVoteSince(request.getBaseUpdate());
			if (result.isReject()) {
				return reject("Rejected by user '" + membership.getUser().getName() + "'.");
			}
		}
		
		return accept("Not rejected by anyone from team '" + getTeam().getName() + ".");
	}

}
