package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteByAuthorizedUser;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
@Editable(order=50, icon="icon-group", description=
		"This gate keeper will be passed if the commit is approved by specified number of users with "
		+ "writing permission.")
public class IfApprovedByProjectWriters extends ApprovalGateKeeper {

    private int leastApprovals = 1;

    @Editable(name="Least Approvals Required")
    @Min(value = 1, message = "Least approvals should not be less than 1.")
    public int getLeastApprovals() {
        return leastApprovals;
    }

    public void setLeastApprovals(int leastApprovals) {
        this.leastApprovals = leastApprovals;
    }

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		AuthorizationManager authorizationManager = Gitop.getInstance(AuthorizationManager.class);
		Collection<User> authorizedUsers = authorizationManager.listAuthorizedUsers(
				request.getTarget().getProject(), GeneralOperation.WRITE);

        int approvals = 0;
        int pendings = 0;
        for (User user: authorizedUsers) {
            Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
            if (result == null) {
                pendings++;
            } else if (result.isAccept()) {
                approvals++;
            }
        }

        if (approvals >= getLeastApprovals()) {
            return accepted("Get at least " + getLeastApprovals() + " approvals from authorized users.");
        } else if (getLeastApprovals() - approvals > pendings) {
            return rejected("Can not get at least " + getLeastApprovals()
                    + " approvals from authorized users.");
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, authorizedUsers, lackApprovals);

            return pending("To be approved by " + lackApprovals + " authorized user(s).", 
            		new CanVoteByAuthorizedUser());
        }
	}
	
	private CheckResult checkApproval(User user, Project project) {
		AuthorizationManager authorizationManager = Gitop.getInstance(AuthorizationManager.class);

		Collection<User> writers = authorizationManager.listAuthorizedUsers(
				project, GeneralOperation.WRITE);

        int approvals = 0;
        int pendings = writers.size();
        
        if (writers.contains(user)) {
        	approvals ++;
        	pendings --;
        }
        
        if (approvals >= leastApprovals) {
            return accepted("Get at least " + leastApprovals + " approvals from authorized users.");
        } else if (leastApprovals - approvals > pendings) {
            return rejected("Can not get at least " + leastApprovals + " approvals from authorized users.");
        } else {
            int lackApprovals = getLeastApprovals() - approvals;
            return pending("Lack " + lackApprovals + " approvals from authorized users.", 
            		new CanVoteByAuthorizedUser());
        }
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkApproval(user, branch.getProject());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkApproval(user, branch.getProject());
	}

	@Override
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return checkApproval(user, project);
	}

}
