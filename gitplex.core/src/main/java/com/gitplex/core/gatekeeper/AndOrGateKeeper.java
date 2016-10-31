package com.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;

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
	public void onAccountRename(String oldName, String newName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onAccountRename(oldName, newName);
	}

	@Override
	public boolean onAccountDelete(String accountName) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onAccountDelete(accountName))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}

	@Override
	public void onDepotRename(Depot renamedDepot, String oldName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onDepotRename(renamedDepot, oldName);
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account originalAccount) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onDepotTransfer(depotDefiningGateKeeper, transferredDepot, originalAccount))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}
	
	@Override
	public boolean onDepotDelete(Depot depot) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onDepotDelete(depot))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}
	
	@Override
	public void onTeamRename(String oldName, String newName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onTeamRename(oldName, newName);
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onTeamDelete(teamName))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}

	@Override
	public boolean onRefDelete(String refName) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onRefDelete(refName))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}
	
}
