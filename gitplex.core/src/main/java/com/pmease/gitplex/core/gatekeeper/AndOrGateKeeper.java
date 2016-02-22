package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
public abstract class AndOrGateKeeper extends CompositeGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Valid
	@NotNull
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

}
