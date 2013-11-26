package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.model.PullRequest;

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
