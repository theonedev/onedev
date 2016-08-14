package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;

@Editable(name="Not Composition", order=400, icon="fa-object-group", category=GateKeeper.CATEGORY_COMPOSITION,
		description="This gate keeper will be passed if contained gate keeper is not passed")
@Horizontal
public class NotGateKeeper extends CompositeGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private GateKeeper gateKeeper = new DefaultGateKeeper();
	
	@Valid
	@NotNull
	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}
	
	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}
	
	@Override
	protected GateCheckResult aggregate(Checker checker) {
		GateCheckResult result = checker.check(getGateKeeper());
		
		if (result instanceof Passed)
			return failed(result.getReasons());
		else if (result instanceof Failed)
			return passed(result.getReasons());
		else
			return result;
	}

	@Override
	public boolean onAccountDelete(String accountName) {
		return gateKeeper.onAccountDelete(accountName);
	}

	@Override
	public boolean onDepotDelete(Depot depot) {
		return gateKeeper.onDepotDelete(depot);
	}

	@Override
	public void onDepotRename(Depot renamedDepot, String oldName) {
		gateKeeper.onDepotRename(renamedDepot, oldName);
	}

	@Override
	public boolean onDepotTransfer(Depot depotDefiningGateKeeper, Depot transferredDepot, 
			Account origninalAccount) {
		return gateKeeper.onDepotTransfer(depotDefiningGateKeeper, transferredDepot, origninalAccount);
	}
	
	@Override
	public void onAccountRename(String oldName, String newName) {
		gateKeeper.onAccountRename(oldName, newName);
	}

	@Override
	public void onTeamRename(String oldName, String newName) {
		gateKeeper.onTeamRename(oldName, newName);
	}

	@Override
	public boolean onTeamDelete(String teamName) {
		return gateKeeper.onTeamDelete(teamName);
	}

	@Override
	public boolean onRefDelete(String refName) {
		return gateKeeper.onRefDelete(refName);
	}

}
