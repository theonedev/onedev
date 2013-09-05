package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.Min;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.TeamMembership;
import com.pmease.gitop.core.model.User;

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
		Collection<User> members = new HashSet<User>();
		for (TeamMembership membership: team.getMemberships())
			members.add(membership.getUser());
		
		boolean pendingBlock = false;
		
		int score = 0;
		int pending = 0;
		for (User member: members) {
			CheckResult result = member.checkApprovalSince(request.getBaseUpdate());
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

		if (score + pending < getMinScore()) {
			return CheckResult.REJECT;
		} else {
			int lackApprovals;
			if (isRequireVoteOfAllMembers()) {
				if (score - pending >= getMinScore())
					return CheckResult.ACCEPT;
				int temp = getMinScore() + pending - score;
				lackApprovals = temp / 2;
				if (temp % 2 != 0)
					lackApprovals++;
				if (lackApprovals > pending)
					lackApprovals = pending;
			} else {
				if (score >= getMinScore())
					return CheckResult.ACCEPT;
				lackApprovals = getMinScore() - score;
			}

			for (int i=0; i<lackApprovals; i++)
				request.requestVote(members);
			
			if (pendingBlock)
				return CheckResult.PENDING_BLOCK;
			else
				return CheckResult.PENDING;
		}
	}

}
