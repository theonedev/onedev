package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public interface GateKeeper extends Trimmable {
	
	public enum CheckResult {ACCEPT, REJECT, UNDETERMINED};
	
	CheckResult check(MergeRequest mergeRequest);

}
