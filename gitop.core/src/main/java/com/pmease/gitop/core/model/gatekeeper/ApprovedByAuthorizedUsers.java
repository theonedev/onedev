package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.permission.operation.Write;

public class ApprovedByAuthorizedUsers extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		OrGateKeeper or = new OrGateKeeper();
		for (User user: request.getDestination().getRepository().findAuthorizedUsers(new Write())) {
			ApprovedBySpecifiedUser entry = new ApprovedBySpecifiedUser();
			entry.setUserId(user.getId());
			or.getGateKeepers().add(entry);
		}		

		return or.check(request);
	}

}
