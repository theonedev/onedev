package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.RepositoryOperation;

@SuppressWarnings("serial")
public class ApprovedByAuthorizedUsers extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		Collection<User> authorizedUsers = request.getDestination().getRepository().findAuthorizedUsers(RepositoryOperation.WRITE);
		OrGateKeeper or = new OrGateKeeper();
		for (User user: authorizedUsers) {
			ApprovedBySpecifiedUser entry = new ApprovedBySpecifiedUser();
			entry.setUserId(user.getId());
			or.getGateKeepers().add(entry);
		}		

		CheckResult result = or.check(request);
		
		if (result.isAccept()) { 
			result = accept("Approved by user with push permission.");
		} else if (result.isReject()) {
			result = reject("Not approved by any users with push permission.");
		} else if (result.isPending()) {
			result = pending("To be approved by someone with push permission.");
		} else {
			result = block("To be approved by someone with push permission.");
		}
		return result;
	}

}
