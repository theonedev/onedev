package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.Write;

public class ApprovedByAuthorizedUsers extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		Collection<User> authorizedUsers = request.getDestination().getRepository().findAuthorizedUsers(new Write());
		OrGateKeeper or = new OrGateKeeper();
		for (User user: authorizedUsers) {
			ApprovedBySpecifiedUser entry = new ApprovedBySpecifiedUser();
			entry.setUserId(user.getId());
			or.getGateKeepers().add(entry);
		}		

		CheckResult result = or.check(request);
		
		if (result.isPending()) {
			request.requestVote(authorizedUsers);
		}
		return result;
	}

}
