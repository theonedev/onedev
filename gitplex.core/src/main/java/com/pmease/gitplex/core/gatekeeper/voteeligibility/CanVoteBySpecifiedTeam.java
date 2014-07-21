package com.pmease.gitplex.core.gatekeeper.voteeligibility;

import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
public class CanVoteBySpecifiedTeam implements VoteEligibility {

    private final Long teamId;
    
    public CanVoteBySpecifiedTeam(Team team) {
        this.teamId = team.getId();
    }
    
    @Override
    public boolean canVote(User user, PullRequest request) {
        for (Membership membership: user.getMemberships()) {
            if (membership.getTeam().getId().equals(teamId))
                return true;
        }
        return false;
    }

}
