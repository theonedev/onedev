package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.InvolvedCommit;

public interface GateKeeper {
	
	public enum CHECK_RESULT {ACCEPT, REJECT, UNDETERMINED}
	
	CHECK_RESULT check(InvolvedCommit commit);
}
