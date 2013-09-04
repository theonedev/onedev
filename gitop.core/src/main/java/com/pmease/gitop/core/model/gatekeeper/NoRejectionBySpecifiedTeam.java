package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.TeamMembership;

public class NoRejectionBySpecifiedTeam extends TeamAwareGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		TeamManager teamManager = AppLoader.getInstance(TeamManager.class);
		Team team = teamManager.load(getTeamId());

		for (TeamMembership membership: team.getMemberships()) {
			CheckResult result = membership.getUser().checkApprovalSince(request.getBaseUpdate());
			if (result == CheckResult.REJECT) {
				return CheckResult.REJECT;
			}
		}
		
		return CheckResult.ACCEPT;
	}

}
