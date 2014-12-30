package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.AuthorizationManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
@Editable(order=50, icon="pa-group-o", description=
		"This gate keeper will be passed if the commit is approved by specified number of users with "
		+ "writing permission.")
public class IfApprovedByRepositoryWriters extends ApprovalGateKeeper {

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
		AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);
		Collection<User> authorizedUsers = authorizationManager.listAuthorizedUsers(
				request.getTarget().getRepository(), GeneralOperation.WRITE);

        int approvals = 0;
        int pendings = 0;
        for (User user: authorizedUsers) {
            Review.Result result = user.checkReviewSince(request.getReferentialUpdate());
            if (result == null) {
                pendings++;
            } else if (result == Review.Result.APPROVE) {
                approvals++;
            }
        }

        if (approvals >= getLeastApprovals()) {
            return passed("Get at least " + getLeastApprovals() + " approvals from authorized users.");
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed("Can not get at least " + getLeastApprovals()
                    + " approvals from authorized users.");
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            request.pickReviewers(authorizedUsers, lackApprovals);

            return pending("To be approved by " + lackApprovals + " authorized user(s).");
        }
	}
	
	private CheckResult check(User user, Repository repository) {
		AuthorizationManager authorizationManager = GitPlex.getInstance(AuthorizationManager.class);

		Collection<User> writers = authorizationManager.listAuthorizedUsers(
				repository, GeneralOperation.WRITE);

        int approvals = 0;
        int pendings = writers.size();
        
        if (writers.contains(user)) {
        	approvals ++;
        	pendings --;
        }
        
        if (approvals >= leastApprovals) {
            return passed("Get at least " + leastApprovals + " approvals from authorized users.");
        } else if (leastApprovals - approvals > pendings) {
            return failed("Can not get at least " + leastApprovals + " approvals from authorized users.");
        } else {
            int lackApprovals = getLeastApprovals() - approvals;
            return pending("Lack " + lackApprovals + " approvals from authorized users.");
        }
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
		return check(user, repository);
	}

}
