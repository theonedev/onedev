package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.annotation.UserChoice;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Review;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.AccountManager;

@Editable(order=200, icon="fa-user", category=GateKeeper.CATEGORY_USER, description=
		"This gate keeper will be passed if the commit is approved by specified user.")
public class IfApprovedBySpecifiedUser extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
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
        Account user = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).findByName(userName));

        Review.Result result = user.checkReviewSince(request.getReferentialUpdate());
        if (result == null) {
            request.pickReviewers(Sets.newHashSet(user), 1);

            return pending(Lists.newArrayList("To be approved by " + user.getDisplayName() + "."));
        } else if (result == Review.Result.APPROVE) {
            return passed(Lists.newArrayList("Approved by " + user.getDisplayName() + "."));
        } else {
            return failed(Lists.newArrayList("Rejected by " + user.getDisplayName() + "."));
        }
    }

    private CheckResult check(Account user) {
        Account approver = Preconditions.checkNotNull(GitPlex.getInstance(AccountManager.class).findByName(userName));
        if (approver.equals(user)) {
        	return passed(Lists.newArrayList("Approved by " + approver.getName() + "."));
        } else {
        	return pending(Lists.newArrayList("Not approved by " + approver.getName() + ".")); 
        }
    }
    
	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return check(user);
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return check(user);
	}

	@Override
	public void onUserRename(String oldName, String newName) {
		if (userName.equals(oldName))
			userName = newName;
	}

	@Override
	public boolean onUserDelete(Account user) {
		return userName.equals(user.getName());
	}

}
