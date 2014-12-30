package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Sets;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=300, icon="pa-user-o", description=
		"This gate keeper will be passed if the commit is approved by owner of the repository.")
public class IfApprovedByRepositoryOwner extends ApprovalGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        User repoOwner = request.getTarget().getRepository().getOwner();

        Review.Result result = repoOwner.checkReviewSince(request.getReferentialUpdate());

        if (result == null) {
            request.pickReviewers(Sets.newHashSet(repoOwner), 1);
            return pending("To be approved by " + repoOwner.getName() + ".");
        } else if (result == Review.Result.APPROVE) {
            return passed("Approved by " + repoOwner.getName() + ".");
        } else {
            return failed("Rejected by " + repoOwner.getName() + ".");
        }
    }

    private CheckResult check(User user, Repository repository) {
		if (user.equals(repository.getOwner()))
			return passed("Approved by repository owner.");
		else
			return pending("Not approved by repository owner.");
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return check(user, branch.getRepository());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return check(user, branch.getRepository());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		if (user.equals(repository.getOwner()))
			return passed("Approved by repository owner.");
		else
			return failed("Not approved by repository owner.");
	}

}
