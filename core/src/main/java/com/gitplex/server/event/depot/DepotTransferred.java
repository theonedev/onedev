package com.gitplex.server.event.depot;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;

public class DepotTransferred {

	private final Depot depot;
	
	private final Account oldAccount;
	
	public DepotTransferred(Depot depot, Account oldAccount) {
		this.depot = depot;
		this.oldAccount = oldAccount;
	}

	public Depot getDepot() {
		return depot;
	}

	public Account getOldAccount() {
		return oldAccount;
	}
	
}
