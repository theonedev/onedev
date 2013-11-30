package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteByAuthorizedUser;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
@Editable
public class ApprovedByAuthorizedUsers extends AbstractGateKeeper {

    private int leastApprovals = 1;

    @Editable
    @Min(value = 1, message = "Least approvals should not be less than 1.")
    public int getLeastApprovals() {
        return leastApprovals;
    }

    public void setLeastApprovals(int leastApprovals) {
        this.leastApprovals = leastApprovals;
    }

	@Override
	public CheckResult check(PullRequest request) {
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

}
