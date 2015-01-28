package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;

@SuppressWarnings("serial")
@Editable(name="If...Then... Composition", order=300, icon="fa-sitemap",  
		category=GateKeeper.CATEGROY_COMPOSITION, 
		description="If first gate keeper is passed, then go ahead to check second gate keeper;")
@Horizontal
public class IfThenGateKeeper extends CompositeGateKeeper {

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

}
