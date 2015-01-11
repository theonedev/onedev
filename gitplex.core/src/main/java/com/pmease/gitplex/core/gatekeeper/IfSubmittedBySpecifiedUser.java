package com.pmease.gitplex.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.UserChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=200, icon="pa-user-o", category=GateKeeper.CATEGROY_CHECK_SUBMITTER, description=
		"This gate keeper will be passed if the commit is submitted by specified user.")
public class IfSubmittedBySpecifiedUser extends AbstractGateKeeper {

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
    	return check(request.getSubmitter());
    }

    @Override
    protected GateKeeper trim(Repository repository) {
        if (GitPlex.getInstance(Dao.class).get(User.class, getUserId()) == null)
            return null;
        else
            return this;
    }

    private CheckResult check(User user) {
		User expectedUser = GitPlex.getInstance(Dao.class).load(User.class, userId);
        if (expectedUser.getId().equals(user.getId())) 
        	return passed(Lists.newArrayList("Submitted by " + expectedUser.getDisplayName() + "."));
        else 
        	return failed(Lists.newArrayList("Not submitted by " + expectedUser.getDisplayName() + ".")); 
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return check(user);
	}

}
