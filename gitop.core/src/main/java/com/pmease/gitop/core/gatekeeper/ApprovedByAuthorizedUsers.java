package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.gatekeeper.voteeligibility.CanVoteByAuthorizedUser;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
@Editable
public class ApprovedByAuthorizedUsers extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		Collection<User> authorizedUsers = request.getTarget().getProject().findAuthorizedUsers(GeneralOperation.WRITE);
		OrGateKeeper or = new OrGateKeeper();
		for (User user: authorizedUsers) {
			ApprovedBySpecifiedUser entry = new ApprovedBySpecifiedUser();
			entry.setUserId(user.getId());
			or.getGateKeepers().add(entry);
		}		

		CheckResult result = or.check(request);
		
		if (result instanceof Accepted) { 
			result = accepted("Approved by user with push permission.");
		} else if (result instanceof Rejected) {
			result = rejected("Not approved by any users with push permission.");
		} else if (result instanceof Blocked) {
			result = blocked("To be approved by someone with push permission.", new CanVoteByAuthorizedUser());
		} else {
			result = pending("To be approved by someone with push permission.", new CanVoteByAuthorizedUser());
		}
		return result;
	}

}
