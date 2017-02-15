package com.gitplex.server.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.entity.PullRequestReview;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.util.editable.annotation.Editable;
import com.google.common.collect.Lists;

@Editable(order=300, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gatekeeper will be passed if the commit gets specified number "
		+ "of disapprovals from specified team. It normally works together "
		+ "with a NOT container to reject the pull request in case there are "
		+ "disapprovals")
public class DisapprovedBySpecifiedTeam extends TeamAwareGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private int leastDisapprovals = 1;

	@Editable(name="Least Disapprovals Required", order=200)
    @Min(value = 1, message = "Least disapprovals should not be less than 1")
	public int getLeastDisapprovals() {
		return leastDisapprovals;
	}

	public void setLeastDisapprovals(int leastDisapprovals) {
		this.leastDisapprovals = leastDisapprovals;
	}

	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
        int disapprovals = 0;
        
    	Collection<Account> members = getTeamMembers(request.getTargetDepot().getAccount());
        for (Account member : members) {
            PullRequestReview.Result result = member.checkReviewSince(request.getReferentialUpdate());
            if (result == PullRequestReview.Result.DISAPPROVE) {
                disapprovals++;
            }
        }

        if (disapprovals >= getLeastDisapprovals()) {
            return passed(Lists.newArrayList("Disapproved by at least " + getLeastDisapprovals() 
            		+ " member(s) of team " + getTeamName()));
        } else {
            return failed(Lists.newArrayList("Not disapproved by at least " + getLeastDisapprovals() 
            		+ " member(s) of team " + getTeamName()));
        }
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return failed(Lists.newArrayList("Not disapproved by anyone from team " + getTeamName()));
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldObjectId,
			ObjectId newObjectId) {
		return failed(Lists.newArrayList("Not disapproved by anyone from team " + getTeamName()));
	}
	
}
