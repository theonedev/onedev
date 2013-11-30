package com.pmease.gitop.model.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable
public class SubmittedViaPush extends AbstractGateKeeper {

	@Override
	public CheckResult check(PullRequest request) {
		if (request.isAutoCreated())
			return accepted("Submitted via push.");
		else
			return rejected("Not submitted via push.");
	}

}
