package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

public class NotGateKeeper implements GateKeeper {

	private GateKeeper gateKeeper;
	
	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}
	
	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}
	
	@Override
	public CheckResult check(MergeRequest request) {
		CheckResult result = getGateKeeper().check(request);
		
		if (result == CheckResult.PENDING)
			return result;
		else if (result == CheckResult.ACCEPT)
			return CheckResult.REJECT;
		else
			return CheckResult.ACCEPT;
	}

	@Override
	public Object trim(Object context) {
		return getGateKeeper().trim(context);
	}

}
