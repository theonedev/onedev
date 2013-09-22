package com.pmease.gitop.core.gatekeeper;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
public class IfThenGateKeeper extends AbstractGateKeeper {

	private GateKeeper ifGate;
	
	private GateKeeper thenGate;
	
	@Editable
	@NotNull
	public GateKeeper getIfGate() {
		return ifGate;
	}

	public void setIfGate(GateKeeper ifGate) {
		this.ifGate = ifGate;
	}

	@Editable
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
		if (ifResult.isAccept()) {
			return getThenGate().check(request);
		} else if (ifResult.isReject()) {
			return accept(ifResult.getReasons());
		} else if (ifResult.isBlock()) {
			return ifResult;
		} else {
			CheckResult thenResult = getThenGate().check(request);
			if (thenResult.isAccept())
				return thenResult;
			else 
				return ifResult;
		}
	}

}
