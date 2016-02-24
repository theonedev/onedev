package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;

@Editable(order=300, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is approved by owner of the repository.")
public class IfApprovedByRepositoryOwner extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
    @Override
    public CheckResult doCheckRequest(PullRequest request) {
        User repoOwner = request.getTargetDepot().getOwner();

        Review.Result result = repoOwner.checkReviewSince(request.getReferentialUpdate());

        if (result == null) {
            request.pickReviewers(Sets.newHashSet(repoOwner), 1);
            return pending(Lists.newArrayList("To be approved by " + repoOwner.getName() + "."));
        } else if (result == Review.Result.APPROVE) {
            return passed(Lists.newArrayList("Approved by " + repoOwner.getName() + "."));
        } else {
            return failed(Lists.newArrayList("Rejected by " + repoOwner.getName() + "."));
        }
    }

    private CheckResult check(User user, Depot depot) {
		if (user.equals(depot.getOwner()))
			return passed(Lists.newArrayList("Approved by repository owner."));
		else
			return pending(Lists.newArrayList("Not approved by repository owner."));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user, depot);
	}

}
