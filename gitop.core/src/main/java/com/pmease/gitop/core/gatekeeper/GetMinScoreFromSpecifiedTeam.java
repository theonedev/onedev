package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedTeam;

@SuppressWarnings("serial")
@Editable(category=GateKeeper.CATEGORY_APPROVAL, order=500, description=
		"This condition will be satisfied if specified score can be get from specified team, the score "
		+ "starts at zero, and will increase by one for each approval, and decrease by one for each "
		+ "rejection.")
public class GetMinScoreFromSpecifiedTeam extends TeamAwareGateKeeper {

    private int minScore = 1;

    private boolean requireVoteOfAllMembers;

    @Editable
    @Min(value = 1, message = "Min score should not be less than 1.")
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
    public CheckResult check(PullRequest request) {
        Collection<User> members = new HashSet<User>();
        for (Membership membership : getTeam().getMemberships())
            members.add(membership.getUser());

        int score = 0;
        int pendings = 0;

        for (User member : members) {
            Vote.Result result = member.checkVoteSince(request.getBaseUpdate());
            if (result == null) {
                pendings++;
            } else if (result.isAccept()) {
                score++;
            } else {
                score--;
            }
        }

        if (score + pendings < getMinScore()) {
            return rejected("Can not get min score " + getMinScore() + " from team '"
                    + getTeam().getName() + "'.");
        } else {
            int lackApprovals;
            if (isRequireVoteOfAllMembers()) {
                if (score - pendings >= getMinScore())
                    return accepted("Get min score " + getMinScore() + " from team '"
                            + getTeam().getName() + "'.");
                int temp = getMinScore() + pendings - score;
                lackApprovals = temp / 2;
                if (temp % 2 != 0) lackApprovals++;
                if (lackApprovals > pendings) lackApprovals = pendings;
            } else {
                if (score >= getMinScore())
                    return accepted("Get min score " + getMinScore() + " from team '"
                            + getTeam().getName() + "'.");
                lackApprovals = getMinScore() - score;
            }

            if (request.getId() != null)
            	Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, members, lackApprovals);

    		String prefix;
    		if (request.getId() == null)
    			prefix = "Not ";
    		else
    			prefix = "To be ";
            return pending(prefix + "approved by " + lackApprovals + " users from team '"
                    + getTeam().getName() + ".", new CanVoteBySpecifiedTeam(getTeam()));
        }
    }

}
