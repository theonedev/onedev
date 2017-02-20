package com.gitplex.server.event.depot;

import com.gitplex.server.model.Depot;

public class DepotDeleted {
	
	private final Depot depot;
	
	public DepotDeleted(Depot depot) {
		this.depot = depot;
	}

	public Depot getDepot() {
		return depot;
	}
	
}
