package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class IsFastForward extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		if (request.isFastForward())
			return accept("Is fast-forward.");
		else
			return reject("None fast-forward.");
	}

}
