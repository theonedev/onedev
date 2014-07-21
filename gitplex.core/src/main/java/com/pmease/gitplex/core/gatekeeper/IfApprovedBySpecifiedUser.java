package com.pmease.gitplex.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.UserChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.Vote;

@SuppressWarnings("serial")
@Editable(order=200, icon="icon-user", description=
		"This gate keeper will be passed if the commit is approved by specified user.")
public class IfApprovedBySpecifiedUser extends ApprovalGateKeeper {

    private Long userId;

    @Editable(name="Select User Below")
    @UserChoice
    @NotNull
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        User user = GitPlex.getInstance(Dao.class).load(User.class, getUserId());

        Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
        if (result == null) {
            request.pickVoters(Sets.newHashSet(user), 1);

            return pending("To be approved by " + user.getDisplayName() + ".",
                    new CanVoteBySpecifiedUser(user));
        } else if (result == Vote.Result.APPROVE) {
            return approved("Approved by " + user.getDisplayName() + ".");
        } else {
            return disapproved("Rejected by " + user.getDisplayName() + ".");
        }
    }

    @Override
    protected GateKeeper trim(Repository repository) {
        if (GitPlex.getInstance(Dao.class).get(User.class, getUserId()) == null)
            return null;
        else
            return this;
    }

    private CheckResult checkApproval(User user) {
		User approver = GitPlex.getInstance(Dao.class).load(User.class, userId);
        if (approver.getId().equals(user.getId())) {
        	return approved("Approved by " + approver.getName() + ".");
        } else {
        	return pending("Not approved by " + approver.getName() + ".", 
        			new CanVoteBySpecifiedUser(approver)); 
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
