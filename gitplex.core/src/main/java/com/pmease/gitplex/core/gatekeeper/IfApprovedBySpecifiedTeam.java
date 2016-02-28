package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;
import java.util.HashSet;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamMembership;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.TeamManager;

@Editable(order=100, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is approved by specified number of users "
		+ "from specified team.")
public class IfApprovedBySpecifiedTeam extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private String teamName;
	
    private int leastApprovals = 1;

    @Editable(name="Team", order=100)
    @NotEmpty
    public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	@Editable(name="Least Approvals Required", order=200)
    @Min(value = 1, message = "Least approvals should not be less than 1.")
    public int getLeastApprovals() {
        return leastApprovals;
    }

    public void setLeastApprovals(int leastApprovals) {
        this.leastApprovals = leastApprovals;
    }

    private Team getTeam(Account owner) {
    	return Preconditions.checkNotNull(GitPlex.getInstance(TeamManager.class).findBy(owner, teamName));
    }
    
    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        Collection<Account> members = new HashSet<Account>();
        for (TeamMembership membership : getTeam(request.getTargetDepot().getOwner()).getMemberships())
            members.add(membership.getUser());

        int approvals = 0;
        int pendings = 0;
        for (Account member : members) {
            Review.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == null) {
                pendings++;
            } else if (result == Review.Result.APPROVE) {
                approvals++;
            }
        }

        if (approvals >= getLeastApprovals()) {
            return passed(Lists.newArrayList("Already get at least " + getLeastApprovals() + " approvals from team '"
                    + teamName + "'."));
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed(Lists.newArrayList("Unable to get at least " + getLeastApprovals()
                    + " approvals from team '" + teamName + "'."));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            request.pickReviewers(members, lackApprovals);

            return pending(Lists.newArrayList("To be approved by " + lackApprovals + " user(s) from team '"
                    + teamName + "'."));
        }
    }

	private CheckResult check(Account user, Account owner) {
        Collection<Account> members = new HashSet<Account>();
        for (TeamMembership membership : getTeam(owner).getMemberships())
            members.add(membership.getUser());

        int approvals = 0;
        int pendings = members.size();
        
        if (members.contains(user)) {
        	approvals ++;
        	pendings --;
        }

        if (approvals >= getLeastApprovals()) {
            return passed(Lists.newArrayList("Get at least " + leastApprovals + " approvals from team '"
                    + teamName + "'."));
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed(Lists.newArrayList("Can not get at least " + leastApprovals 
                    + " approvals from team '" + teamName + "'."));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            return pending(Lists.newArrayList("Lack " + lackApprovals + " approvals from team '"
                    + teamName + "'."));
        }
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot.getOwner());
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return check(user, depot.getOwner());
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		if (teamName.equals(oldName))
			teamName = newName;
	}

	@Override
	public boolean onTeamDelete(Team team) {
		return teamName.equals(team.getName());
	}
	
}
