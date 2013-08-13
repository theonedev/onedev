package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public interface GateKeeper {
	
	public enum CheckResult {ACCEPT, REJECT, UNDETERMINED};
	
	CheckResult check(MergeRequest mergeRequest);

}
