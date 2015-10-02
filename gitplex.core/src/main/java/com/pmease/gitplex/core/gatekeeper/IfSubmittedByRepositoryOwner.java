package com.pmease.gitplex.core.gatekeeper;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=300, icon="fa-user", category=GateKeeper.CATEGROY_CHECK_SUBMITTER, description=
		"This gate keeper will be passed if the commit is submitted by owner of the repository.")
public class IfSubmittedByRepositoryOwner extends AbstractGateKeeper {

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter(), request.getTargetRepo());
    }

    private CheckResult check(User user, Repository repository) {
		if (user.equals(repository.getOwner()))
			return passed(Lists.newArrayList("Submitted by repository owner."));
		else
			return failed(Lists.newArrayList("Not submitted by repository owner."));
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Repository repository, String branch, String file) {
		return check(user, repository);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Repository repository, String branch, String commit) {
		return check(user, repository);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return check(user, repository);
	}

}
