package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.UserChoice;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;

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
    	if (request.isNew())
    		return checkApproval(request.getSubmitter());
    	
        UserManager userManager = Gitop.getInstance(UserManager.class);
        User user = userManager.load(getUserId());

        Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
        if (result == null) {
            Gitop.getInstance(VoteInvitationManager.class).inviteToVote(request, Sets.newHashSet(user), 1);

            return pending("To be approved by user '" + user.getName() + "'.",
                    new CanVoteBySpecifiedUser(user));
        } else if (result.isAccept()) {
            return accepted("Approved by user '" + user.getName() + "'.");
        } else {
            return rejected("Rejected by user '" + user.getName() + "'.");
        }
    }

    @Override
    protected GateKeeper trim(Project project) {
        if (Gitop.getInstance(UserManager.class).get(getUserId()) == null)
            return null;
        else
            return this;
    }

    private CheckResult checkApproval(User user) {
		User approver = Gitop.getInstance(UserManager.class).load(userId);
        if (approver.getId().equals(user.getId())) {
        	return accepted("Approved by user '" + approver.getName() + "'.");
        } else {
        	return pending("Not approved by user '" + approver.getName() + "'.", 
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
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return checkApproval(user);
	}

}
