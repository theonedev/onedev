package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.Review;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.operation.DepotOperation;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
@Editable(order=50, icon="fa-group", category=GateKeeper.CATEGROY_CHECK_REVIEW, description=
		"This gate keeper will be passed if the commit is approved by specified number of users with "
		+ "writing permission.")
public class IfApprovedByRepositoryWriters extends AbstractGateKeeper {

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
		Collection<User> authorizedUsers = SecurityUtils.findUsersCan(
				request.getTargetDepot(), DepotOperation.PUSH);

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
            return passed(Lists.newArrayList("Get at least " + getLeastApprovals() + " approvals from authorized users."));
        } else if (getLeastApprovals() - approvals > pendings) {
            return failed(Lists.newArrayList("Can not get at least " + getLeastApprovals()
                    + " approvals from authorized users."));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;

            request.pickReviewers(authorizedUsers, lackApprovals);

            return pending(Lists.newArrayList("To be approved by " + lackApprovals + " authorized user(s)."));
        }
	}
	
	private CheckResult check(User user, Depot depot) {
		Collection<User> writers = SecurityUtils.findUsersCan(depot, DepotOperation.PUSH);

        int approvals = 0;
        int pendings = writers.size();
        
        if (writers.contains(user)) {
        	approvals ++;
        	pendings --;
        }
        
        if (approvals >= leastApprovals) {
            return passed(Lists.newArrayList("Get at least " + leastApprovals + " approvals from authorized users."));
        } else if (leastApprovals - approvals > pendings) {
            return failed(Lists.newArrayList("Can not get at least " + leastApprovals + " approvals from authorized users."));
        } else {
            int lackApprovals = getLeastApprovals() - approvals;
            return pending(Lists.newArrayList("Lack " + lackApprovals + " approvals from authorized users."));
        }
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return check(user, depot);
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return check(user, depot);
	}

}
