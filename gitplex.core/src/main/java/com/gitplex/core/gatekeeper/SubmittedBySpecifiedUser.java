package com.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.core.GitPlex;
import com.gitplex.core.annotation.AccountChoice;
import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.core.manager.AccountManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(order=600, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gatekeeper will be passed if the commit is submitted by specified user")
public class SubmittedBySpecifiedUser extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
    private String userName;

    @Editable(name="Select User")
    @AccountChoice
    @NotEmpty
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public GateCheckResult doCheckRequest(PullRequest request) {
    	return checkSubmitter(request.getSubmitter());
    }

    private GateCheckResult checkSubmitter(Account user) {
		Account expectedUser = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).findByName(userName));
        if (expectedUser.equals(user)) 
        	return passed(Lists.newArrayList("Submitted by " + expectedUser.getDisplayName()));
        else 
        	return failed(Lists.newArrayList("Not submitted by " + expectedUser.getDisplayName())); 
    }
    
	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return checkSubmitter(user);
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
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
