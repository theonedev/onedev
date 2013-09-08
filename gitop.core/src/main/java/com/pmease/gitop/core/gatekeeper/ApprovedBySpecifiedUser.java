package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.EasySet;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;

@SuppressWarnings("serial")
public class ApprovedBySpecifiedUser extends AbstractGateKeeper {

	private Long userId;
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		UserManager userManager = AppLoader.getInstance(UserManager.class);
		User user = userManager.load(getUserId());

		Vote.Result result = user.checkVoteSince(request.getBaseUpdate());
		if (result == null) {
			request.inviteToVote(EasySet.of(user), 1);
			return pending("To be approved by user '" + user.getName() + "'.");
		} else if (result.isAccept()) {
			return accept("Approved by user '" + user.getName() + "'.");
		} else {
			return reject("Rejected by user '" + user.getName() + "'.");
		}
	}

	@Override
	public Object trim(Object context) {
		UserManager userManager = AppLoader.getInstance(UserManager.class);
		if (userManager.get(getUserId()) == null)
			return null;
		else
			return this;
	}

}
