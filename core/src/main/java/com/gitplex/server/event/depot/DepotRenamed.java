package com.gitplex.server.event.depot;

import com.gitplex.server.entity.Depot;

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
