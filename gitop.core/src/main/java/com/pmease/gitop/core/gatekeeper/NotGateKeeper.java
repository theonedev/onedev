package com.pmease.gitop.core.gatekeeper;

import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
public class NotGateKeeper extends AbstractGateKeeper {

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
		
		if (result.isAccept())
			return reject(result.getReasons());
		else if (result.isReject())
			return accept(result.getReasons());
		else
			return result;
	}

	@Override
	public Object trim(Object context) {
		return getGateKeeper().trim(context);
	}

}
