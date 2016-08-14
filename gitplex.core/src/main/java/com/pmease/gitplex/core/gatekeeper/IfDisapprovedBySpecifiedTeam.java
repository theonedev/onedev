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

@Editable(order=300, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit gets specified number "
		+ "of disapprovals from specified team. It normally works together "
		+ "with a NOT container to reject the pull request in case there are "
		+ "disapprovals")
public class IfDisapprovedBySpecifiedTeam extends TeamAwareGateKeeper {

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
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit,
			ObjectId newCommit) {
		return failed(Lists.newArrayList("Not disapproved by anyone from team " + getTeamName()));
	}
	
}
