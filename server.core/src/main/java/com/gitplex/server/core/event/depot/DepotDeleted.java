package com.gitplex.server.core.event.depot;

import com.gitplex.server.core.entity.Depot;

public class DepotDeleted {
	
	private final Depot depot;
	
	public DepotDeleted(Depot depot) {
		this.depot = depot;
	}

	public Depot getDepot() {
		return depot;
	}
	
}
