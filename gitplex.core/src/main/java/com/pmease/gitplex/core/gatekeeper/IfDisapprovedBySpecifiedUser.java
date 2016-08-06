package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.AccountChoice;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestReview;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.AccountManager;

@Editable(order=300, icon="fa-group", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is disapproved by specified user. "
		+ "It normally works together with a NOT container to reject the pull request "
		+ "in case the user disapproved it")
public class IfDisapprovedBySpecifiedUser extends AbstractGateKeeper {

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
	protected CheckResult doCheckRequest(PullRequest request) {
		Account user = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).find(userName));
		PullRequestReview.Result result = user.checkReviewSince(request.getReferentialUpdate());
		if (result == PullRequestReview.Result.DISAPPROVE) {
            return passed(Lists.newArrayList("Disapproved by " + userName));
		} else {
            return failed(Lists.newArrayList("Not disapproved by " + userName));
		}
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return failed(Lists.newArrayList("Not disapproved by " + userName));
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit,
			ObjectId newCommit) {
		return failed(Lists.newArrayList("Not disapproved by " + userName));
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
