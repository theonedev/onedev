package com.gitplex.core.event.depot;

import com.gitplex.core.entity.Depot;

public class DepotRenamed {
	
	private final Depot depot;
	
	private final String oldName;
	
	public DepotRenamed(Depot depot, String oldName) {
		this.depot = depot;
		this.oldName = oldName;
	}

	public Depot getDepot() {
		return depot;
	}

	public String getOldName() {
		return oldName;
	}
	
}
