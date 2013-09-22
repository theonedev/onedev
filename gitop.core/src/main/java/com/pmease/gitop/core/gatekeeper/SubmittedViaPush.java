package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class SubmittedViaPush extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.isAutoCreated())
			return accept("Submitted via push.");
		else
			return reject("Not submitted via push.");
	}

}
