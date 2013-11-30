package com.pmease.gitop.model.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable
public class HasSourceBranch extends AbstractGateKeeper {

	@Override
	public CheckResult check(PullRequest request) {
		if (request.getSource() != null)
			return accepted("Associated with source branch.");
		else
			return rejected("Not associated with source branch.");
	}

}
