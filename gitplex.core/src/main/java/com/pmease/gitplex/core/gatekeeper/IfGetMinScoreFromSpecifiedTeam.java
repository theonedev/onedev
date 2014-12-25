package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=500, icon="fa-group-o", description=
		"This gate keeper will be passed if specified score can be get from specified team, the score "
		+ "starts at zero, and will increase by one for each approval, and decrease by one for each "
		+ "rejection.")
public class IfGetMinScoreFromSpecifiedTeam extends TeamAwareGateKeeper {

    private int minScore = 1;

    private boolean requireVoteOfAllMembers;

    @Editable(name="Minimum Score Required", order=100, description=
    		"Score is calculated by increasing by one for each approval, and decreasing by one for "
    		+ "each rejection.")
    @Min(value = 1, message = "Min score should not be less than 1.")
    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    @Editable(name="Require Vote of All Members", order=200, description=
    		"Whether or not to require vote of all members in the team before checking score.")
    public boolean isRequireVoteOfAllMembers() {
        return requireVoteOfAllMembers;
    }

    public void setRequireVoteOfAllMembers(boolean requireVoteOfAllMembers) {
        this.requireVoteOfAllMembers = requireVoteOfAllMembers;
    }

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        Collection<User> members = new HashSet<User>();
        for (Membership membership : getTeam().getMemberships())
            members.add(membership.getUser());

        int score = 0;
        int pendings = 0;

        for (User member : members) {
            Review.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == null) {
                pendings++;
            } else if (result == Review.Result.APPROVE) {
                score++;
            } else {
                score--;
            }
        }

        int lackApprovals = calcLackApprovals(score, pendings);

        if (lackApprovals == 0) {
            return approved("Get min score " + getMinScore() + " from team '" + getTeam().getName() + "'.");
        } else if (lackApprovals < 0) {
            return disapproved("Can not get min score " + getMinScore() + " from team '" + getTeam().getName() + "'.");
        } else {
            request.pickReviewers(members, lackApprovals);

            return pending("To be approved by " + lackApprovals + " users from team '"
                    + getTeam().getName() + ".");
        }
    }

    private int calcLackApprovals(int score, int pendings) {
        if (score + pendings < getMinScore()) {
            return -1;
        } else {
            int lackApprovals;
            if (isRequireVoteOfAllMembers()) {
                if (score - pendings >= getMinScore())
                    return 0;
                int temp = getMinScore() + pendings - score;
                lackApprovals = temp / 2;
                if (temp % 2 != 0) lackApprovals++;
                if (lackApprovals > pendings) lackApprovals = pendings;
            } else {
                if (score >= getMinScore())
                    return 0;
                lackApprovals = getMinScore() - score;
            }

            return lackApprovals;
        }
    }

	private CheckResult checkApproval(User user) {
        Collection<User> members = new HashSet<User>();
        for (Membership membership : getTeam().getMemberships())
            members.add(membership.getUser());

        int score = 0;
        int pendings = members.size();
        
        if (members.contains(user)) {
        	score ++;
        	pendings --;
        }

        int lackApprovals = calcLackApprovals(score, pendings);

        if (lackApprovals == 0) {
            return approved("Get min score " + getMinScore() + " from team '" + getTeam().getName() + "'.");
        } else if (lackApprovals < 0) {
            return disapproved("Can not get min score " + getMinScore() + " from team '" + getTeam().getName() + "'.");
        } else {
            return pending("Lack " + lackApprovals + " approvals from team '"
                    + getTeam().getName() + ".");
        }
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkApproval(user);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkApproval(user);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return checkApproval(user);
	}

}
