package com.pmease.gitop.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;

@SuppressWarnings("serial")
@Editable(order=310, icon="icon-user", description=
		"This gate keeper will be passed if the commit is approved by creator of target branch.")
public class IfApprovedByBranchCreator extends ApprovalGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        User branchCreator = request.getTarget().getCreator();

        if (branchCreator != null) {
	        Vote.Result result = branchCreator.checkVoteSince(request.getBaseUpdate());
	
	        if (result == null) {
	            request.pickVoters(Sets.newHashSet(branchCreator), 1);
	            return pending("To be approved by user '" + branchCreator.getName() + "'.",
	                    new CanVoteBySpecifiedUser(branchCreator));
	        } else if (result == Vote.Result.APPROVE) {
	            return approved("Approved by user '" + branchCreator.getName() + "'.");
	        } else {
	            return disapproved("Rejected by user '" + branchCreator.getName() + "'.");
	        }
        } else {
        	return disapproved("Rejected as branch creator is unknown.");
        }
    }

    private CheckResult checkApproval(User user, Branch branch) {
    	if (branch.getCreator() != null) {
			if (user.equals(branch.getCreator()))
				return approved("Approved by branch creator.");
			else
				return pending("Not approved by branch creator.", new CanVoteBySpecifiedUser(branch.getCreator()));
    	} else {
    		return disapproved("Rejected as branch creator is unknown.");
    	}
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkApproval(user, branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkApproval(user, branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
