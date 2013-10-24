package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class AlwaysAccept extends AbstractGateKeeper {

	@Override
	public CheckResult check(MergeRequest request) {
		return accepted("always");
	}

}
