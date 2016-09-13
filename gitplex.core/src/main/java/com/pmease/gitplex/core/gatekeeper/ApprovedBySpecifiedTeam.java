package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;

@Editable(order=100, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gatekeeper will be passed if the commit is approved by specified number of users "
		+ "from specified team")
public class ApprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	private static final long serialVersionUID = 1L;
	
    private int leastApprovals = 1;

	@Editable(name="Least Approvals Required", order=200)
    @Min(value = 1, message = "Least approvals should not be less than 1")
    public int getLeastApprovals() {
        return leastApprovals;
    }

    public void setLeastApprovals(int leastApprovals) {
        this.leastApprovals = leastApprovals;
    }
    
    @Override
    public GateCheckResult doCheckRequest(PullRequest request) {
        int approvals = 0;
        int pendings = 0;
        
    	Collection<Account> members = getTeamMembers(request.getTargetDepot().getAccount());
        for (Account member: members) {
            PullRequestReview.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == null) {
                pendings++;
            } else if (result == PullRequestReview.Result.APPROVE) {
                approvals++;
            }
        }

        if (approvals >= getLeastApprovals()) {
            return passed(Lists.newArrayList("Approved by at least " + getLeastApprovals() 
            		+ " member(s) of team " + getTeamName()));
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed(Lists.newArrayList("Unable to get at least " + getLeastApprovals()
                    + " approvals from team " + getTeamName()));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            request.pickReviewers(members, lackApprovals);

            return pending(Lists.newArrayList("To be approved by " + lackApprovals 
            		+ " member(s) from team " + getTeamName()));
        }
    }

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
        return pending(Lists.newArrayList("Need to be approved by at least " + getLeastApprovals()
        			+ " member(s) of team " + getTeamName()));
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
        return pending(Lists.newArrayList("Need to be approved by at least " + getLeastApprovals()
        			+ " member(s) of team " + getTeamName()));
	}

}
