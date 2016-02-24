package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.gitplex.core.entity.Depot;

public abstract class AndOrGateKeeper extends CompositeGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Valid
	@NotNull
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	public void onDepotNameChange(Depot depot, String oldName, String newName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onDepotNameChange(depot, oldName, newName);
	}

}
