package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Disapproved;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
@Editable(name="If Contained Gate Keeper Is Not Passed", order=400, icon="icon-servers",  
		description="This gate keeper will be passed if contained gate keeper is not passed.")
@Horizontal
public class NotGateKeeper extends CompositeGateKeeper {

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
	protected GateKeeper trim(Repository repository) {
		return (GateKeeper) getGateKeeper().trim(repository);
	}

	@Override
	protected CheckResult aggregate(Checker checker) {
		CheckResult result = checker.check(getGateKeeper());
		
		if (result instanceof Approved)
			return disapproved(result.getReasons());
		else if (result instanceof Disapproved)
			return approved(result.getReasons());
		else
			return result;
	}

}
