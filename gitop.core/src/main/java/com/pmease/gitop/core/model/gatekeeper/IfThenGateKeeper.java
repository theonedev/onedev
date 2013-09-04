package com.pmease.gitop.core.model.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.gitop.core.model.MergeRequest;

public class IfThenGateKeeper extends AbstractGateKeeper {

	private GateKeeper ifGate;
	
	private GateKeeper thenGate;
	
	@NotNull
	public GateKeeper getIfGate() {
		return ifGate;
	}

	public void setIfGate(GateKeeper ifGate) {
		this.ifGate = ifGate;
	}

	@NotNull
	public GateKeeper getThenGate() {
		return thenGate;
	}

	public void setThenGate(GateKeeper thenGate) {
		this.thenGate = thenGate;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		CheckResult ifResult = getIfGate().check(request);
		if (ifResult == CheckResult.ACCEPT) {
			return getThenGate().check(request);
		} else if (ifResult == CheckResult.REJECT) {
			return CheckResult.ACCEPT;
		} else if (ifResult == CheckResult.PENDING_BLOCK) {
			return CheckResult.PENDING_BLOCK;
		} else {
			CheckResult thenResult = getThenGate().check(request);
			if (thenResult == CheckResult.ACCEPT)
				return CheckResult.ACCEPT;
			else 
				return CheckResult.PENDING;
		}
	}

}
