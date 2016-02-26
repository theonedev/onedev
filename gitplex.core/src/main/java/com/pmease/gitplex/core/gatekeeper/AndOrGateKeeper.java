package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.User;

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
	public void onUserRename(String oldName, String newName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onUserRename(oldName, newName);
	}

	@Override
	public boolean onUserDelete(User user) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onUserDelete(user))
				it.remove();
		}
		return gateKeepers.isEmpty();
	}

	@Override
	public void onDepotRename(User depotOwner, String oldName, String newName) {
		for (GateKeeper gateKeeper: gateKeepers)
			gateKeeper.onDepotRename(depotOwner, oldName, newName);
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
	public boolean onTeamDelete(Team team) {
		for (Iterator<GateKeeper> it = gateKeepers.iterator(); it.hasNext();) {
			if (it.next().onTeamDelete(team))
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
