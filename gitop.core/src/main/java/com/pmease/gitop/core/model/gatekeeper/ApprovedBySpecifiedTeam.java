package com.pmease.gitop.core.model.gatekeeper;

import javax.validation.constraints.Min;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.TeamMembership;

public class ApprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	private int leastApprovals = 1;
	
	@Min(1)
	public int getLeastApprovals() {
		return leastApprovals;
	}

	public void setLeastApprovals(int leastApprovals) {
		this.leastApprovals = leastApprovals;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		TeamManager teamManager = AppLoader.getInstance(TeamManager.class);
		Team team = teamManager.load(getTeamId());

		boolean pendingBlock = false;
		
		int approvals = 0;
		int pendings = 0;
		for (TeamMembership membership: team.getMemberships()) {
			CheckResult result = membership.getUser().checkApprovalSince(request.getBaseUpdate());
			if (result == CheckResult.ACCEPT) {
				approvals++;
			} else if (result == CheckResult.PENDING_BLOCK) {
				pendingBlock = true;
				pendings++;
			} else if (result == CheckResult.PENDING) {
				pendings++;
			}
		}
		
		if (approvals >= getLeastApprovals())
			return CheckResult.ACCEPT;
		else if (getLeastApprovals() - approvals > pendings)
			return CheckResult.REJECT;
		else if (pendingBlock)
			return CheckResult.PENDING_BLOCK;
		else
			return CheckResult.PENDING;
	}

}
