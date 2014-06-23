package com.pmease.gitop.core.extensions.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.ApprovalGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.CanVoteBySpecifiedUser;

@SuppressWarnings("serial")
@Editable(order=300, icon="icon-user", description=
		"This gate keeper will be passed if the commit is approved by owner of the repository.")
public class IfApprovedByRepositoryOwner extends ApprovalGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        User repoOwner = request.getTarget().getRepository().getOwner();

        Vote.Result result = repoOwner.checkVoteSince(request.getBaseUpdate());

        if (result == null) {
            request.pickVoters(Sets.newHashSet(repoOwner), 1);
            return pending("To be approved by " + repoOwner.getName() + ".",
                    new CanVoteBySpecifiedUser(repoOwner));
        } else if (result == Vote.Result.APPROVE) {
            return approved("Approved by " + repoOwner.getName() + ".");
        } else {
            return disapproved("Rejected by " + repoOwner.getName() + ".");
        }
    }

    private CheckResult checkApproval(User user, Repository repository) {
		if (user.equals(repository.getOwner()))
			return approved("Approved by repository owner.");
		else
			return pending("Not approved by repository owner.", new CanVoteBySpecifiedUser(repository.getOwner()));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkApproval(user, branch.getRepository());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkApproval(user, branch.getRepository());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		if (user.equals(repository.getOwner()))
			return approved("Approved by repository owner.");
		else
			return disapproved("Not approved by repository owner.");
	}

}
