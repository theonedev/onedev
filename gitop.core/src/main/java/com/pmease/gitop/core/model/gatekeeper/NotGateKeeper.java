package com.pmease.gitop.core.model.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public class NotGateKeeper implements GateKeeper {

	private GateKeeper gateKeeper;

	@NotNull
	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}

	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		CheckResult result = gateKeeper.check(mergeRequest);
		
		if (result == CheckResult.UNDETERMINED)
			return result;
		else if (result == CheckResult.ACCEPT)
			return CheckResult.REJECT;
		else
			return CheckResult.ACCEPT;
	}

	@Override
	public Trimmable trim() {
		return gateKeeper.trim();
	}

}
