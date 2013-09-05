package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.EasySet;
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

		CheckResult result = user.checkApprovalSince(request.getBaseUpdate());
		if (result.isPending())
			request.requestVote(EasySet.of(user));
		return result;
		
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
