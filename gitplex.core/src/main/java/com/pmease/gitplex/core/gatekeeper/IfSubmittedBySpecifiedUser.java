package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.AccountChoice;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.AccountManager;

@Editable(order=600, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is submitted by specified user")
public class IfSubmittedBySpecifiedUser extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
    private String userName;

    @Editable(name="Select User Below")
    @AccountChoice
    @NotEmpty
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public CheckResult doCheckRequest(PullRequest request) {
    	return checkSubmitter(request.getSubmitter());
    }

    private CheckResult checkSubmitter(Account user) {
		Account expectedUser = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).find(userName));
        if (expectedUser.equals(user)) 
        	return passed(Lists.newArrayList("Submitted by " + expectedUser.getDisplayName()));
        else 
        	return failed(Lists.newArrayList("Not submitted by " + expectedUser.getDisplayName())); 
    }
    
	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return checkSubmitter(user);
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return checkSubmitter(user);
	}

	@Override
	public void onAccountRename(String oldName, String newName) {
		if (userName.equals(oldName))
			userName = newName;
	}

	@Override
	public boolean onAccountDelete(String accountName) {
		return userName.equals(accountName);
	}

}
