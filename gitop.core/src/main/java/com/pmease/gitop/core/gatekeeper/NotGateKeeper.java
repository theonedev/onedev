package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class NotGateKeeper extends AbstractGateKeeper {

	private GateKeeper gateKeeper;
	
	@Editable
	@NotNull
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
