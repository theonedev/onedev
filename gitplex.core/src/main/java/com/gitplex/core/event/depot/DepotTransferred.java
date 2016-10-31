package com.gitplex.core.event.depot;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;

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
