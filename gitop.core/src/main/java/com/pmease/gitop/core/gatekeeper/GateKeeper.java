package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public interface GateKeeper extends Trimmable {
	
	CheckResult check(MergeRequest request);
}
