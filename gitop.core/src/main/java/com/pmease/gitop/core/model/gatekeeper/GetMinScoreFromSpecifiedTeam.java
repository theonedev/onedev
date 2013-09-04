package com.pmease.gitop.core.model.gatekeeper;

import javax.validation.constraints.Min;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.TeamMembership;

public class GetMinScoreFromSpecifiedTeam extends TeamAwareGateKeeper {

	private int minScore = 1;
	
	private boolean requireVoteOfAllMembers;
	
	@Min(1)
	public int getMinScore() {
		return minScore;
	}

	public void setMinScore(int minScore) {
		this.minScore = minScore;
	}

	public boolean isRequireVoteOfAllMembers() {
		return requireVoteOfAllMembers;
	}

	public void setRequireVoteOfAllMembers(boolean requireVoteOfAllMembers) {
		this.requireVoteOfAllMembers = requireVoteOfAllMembers;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		TeamManager teamManager = Gitop.getInstance(TeamManager.class);
		Team team = teamManager.load(getTeamId());

		boolean pendingBlock = false;
		
		int score = 0;
		int pending = 0;
		for (TeamMembership membership: team.getMemberships()) {
			CheckResult result = membership.getUser().checkApprovalSince(request.getBaseUpdate());
			if (result == CheckResult.ACCEPT) {
				score++;
			} else if (result == CheckResult.REJECT) {
				score--;
			} else if (result == CheckResult.PENDING_BLOCK) {
				pendingBlock = true;
				pending++;
			} else {
				pending++;
			}
		}

		if (score + pending < getMinScore())
			return CheckResult.REJECT;
		else if (score - pending >= getMinScore())
			return CheckResult.ACCEPT;
		else if (pendingBlock)
			return CheckResult.PENDING_BLOCK;
		else
			return CheckResult.PENDING;
	}

}
