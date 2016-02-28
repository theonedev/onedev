package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;

@Editable(name="If...Then... Composition", order=300, icon="fa-object-group",  
		category=GateKeeper.CATEGORY_COMPOSITION, 
		description="If first gate keeper is passed, then go ahead to check second gate keeper;")
@Horizontal
public class IfThenGateKeeper extends CompositeGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private GateKeeper ifGate = new DefaultGateKeeper();
	
	private GateKeeper thenGate = new DefaultGateKeeper();
	
	@Valid
	@NotNull
	public GateKeeper getIfGate() {
		return ifGate;
	}

	public void setIfGate(GateKeeper ifGate) {
		this.ifGate = ifGate;
	}

	@Valid
	@NotNull
	public GateKeeper getThenGate() {
		return thenGate;
	}

	public void setThenGate(GateKeeper thenGate) {
		this.thenGate = thenGate;
	}

	@Override
	protected CheckResult aggregate(Checker checker) {
		CheckResult ifResult = checker.check(getIfGate());
		if (ifResult instanceof Passed) {
			return checker.check(getThenGate());
		} else if (ifResult instanceof Failed) {
			return passed(ifResult.getReasons());
		} else if (ifResult instanceof Blocking) {
			return ifResult;
		} else if (ifResult instanceof Pending) {
			CheckResult thenResult = checker.check(getThenGate());
			if (thenResult instanceof Passed)
				return thenResult;
			else 
				return ifResult;
		} else {
			return ifResult;
		}
	}

	@Override
	public void onDepotRename(Account depotOwner, String oldName, String newName) {
		ifGate.onDepotRename(depotOwner, oldName, newName);
		thenGate.onDepotRename(depotOwner, oldName, newName);
	}

	@Override
	public void onUserRename(String oldName, String newName) {
		ifGate.onUserRename(oldName, newName);
		thenGate.onUserRename(oldName, newName);
	}

	@Override
	public boolean onUserDelete(Account user) {
		return ifGate.onUserDelete(user) || thenGate.onUserDelete(user);
	}

	@Override
	public boolean onDepotDelete(Depot depot) {
		return ifGate.onDepotDelete(depot) || thenGate.onDepotDelete(depot);
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		ifGate.onTeamRename(oldName, newName);
		thenGate.onTeamRename(oldName, newName);
	}

	@Override
	public boolean onTeamDelete(Team team) {
		return ifGate.onTeamDelete(team) || thenGate.onTeamDelete(team);
	}

	@Override
	public boolean onRefDelete(String refName) {
		return ifGate.onRefDelete(refName) || thenGate.onRefDelete(refName);
	}

}
