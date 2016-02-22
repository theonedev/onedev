package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.UserChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=600, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is submitted by specified user.")
public class IfSubmittedBySpecifiedUser extends AbstractGateKeeper {

    private String userName;

    @Editable(name="Select User Below")
    @UserChoice
    @NotEmpty
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return check(request.getSubmitter());
    }

    private CheckResult check(User user) {
		User expectedUser = Preconditions.checkNotNull(GitPlex.getInstance(UserManager.class).findByName(userName));
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
