package com.pmease.gitop.core.gatekeeper.voteeligibility;

import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;

@SuppressWarnings("serial")
public class CanVoteBySpecifiedTeam implements VoteEligibility {

    private final Long teamId;
    
    public CanVoteBySpecifiedTeam(Team team) {
        this.teamId = team.getId();
    }
    
    @Override
    public boolean canVote(User user, PullRequest request) {
        for (Team team: user.getTeams()) {
            if (team.getId().equals(teamId))
                return true;
        }
        return false;
    }

}
