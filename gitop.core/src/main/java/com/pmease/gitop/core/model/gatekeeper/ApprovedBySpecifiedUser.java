package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;

public class ApprovedBySpecifiedUser implements GateKeeper {

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

		return user.checkApprovalSince(request.getBaseUpdate());
	}

	@Override
	public Object trim(Object context) {
		UserManager userManager = AppLoader.getInstance(UserManager.class);
		if (userManager.lookup(getUserId()) == null)
			return null;
		else
			return this;
	}

}
