package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Membership;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

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
    @Min(value = 1, message = "Least approvals should not be less than 1")
    public int getLeastApprovals() {
        return leastApprovals;
    }

    public void setLeastApprovals(int leastApprovals) {
        this.leastApprovals = leastApprovals;
    }
    
    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        int approvals = 0;
        int pendings = 0;
        
    	Collection<Account> members = new ArrayList<>();
    	for (Membership membership: request.getTargetDepot().getOwner().getUserMemberships()) {
    		if (membership.getJoinedTeams().contains(teamName))
    			members.add(membership.getUser());
    	}
        
        for (Account member : members) {
            Review.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == null) {
                pendings++;
            } else if (result == Review.Result.APPROVE) {
                approvals++;
            }
        }

        if (approvals >= getLeastApprovals()) {
            return passed(Lists.newArrayList("Approved by at least " + getLeastApprovals() 
            		+ " member(s) of team " + teamName));
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed(Lists.newArrayList("Unable to get at least " + getLeastApprovals()
                    + " approvals from team " + teamName));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            request.pickReviewers(members, lackApprovals);

            return pending(Lists.newArrayList("To be approved by " + lackApprovals 
            		+ " member(s) from team " + teamName));
        }
    }

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
        return pending(Lists.newArrayList("Need to be approved by at least " + getLeastApprovals()
        			+ " member(s) of team " + teamName));
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
        return pending(Lists.newArrayList("Need to be approved by at least " + getLeastApprovals()
        			+ " member(s) of team " + teamName));
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		if (teamName.equals(oldName))
			teamName = newName;
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		return this.teamName.equals(teamName);
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalOwner) {
		return depotDefiningGateKeeper.equals(transferredDepot);
	}

}
