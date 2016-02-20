package com.pmease.gitplex.core.gatekeeper;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.util.editable.UserChoice;

@SuppressWarnings("serial")
@Editable(order=600, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
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
    protected GateKeeper trim(Depot depot) {
        if (GitPlex.getInstance(Dao.class).get(User.class, getUserId()) == null)
            return null;
        else
            return this;
    }

    private CheckResult check(User user) {
		User expectedUser = GitPlex.getInstance(Dao.class).load(User.class, userId);
        if (expectedUser.equals(user)) 
        	return passed(Lists.newArrayList("Submitted by " + expectedUser.getDisplayName() + "."));
        else 
        	return failed(Lists.newArrayList("Not submitted by " + expectedUser.getDisplayName() + ".")); 
    }
    
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user);
	}

}
