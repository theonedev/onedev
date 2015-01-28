package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
@Editable(name="Not Composition", order=400, icon="fa-sitemap", category=GateKeeper.CATEGROY_COMPOSITION,
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
		
		if (result instanceof Passed)
			return failed(result.getReasons());
		else if (result instanceof Failed)
			return passed(result.getReasons());
		else
			return result;
	}

}
