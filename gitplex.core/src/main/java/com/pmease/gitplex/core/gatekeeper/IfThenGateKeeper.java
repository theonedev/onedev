package com.pmease.gitplex.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.gatekeeper.checkresult.PendingAndBlock;

@SuppressWarnings("serial")
@Editable(name="Check Second Gate Keeper If First Gate Keeper Is Passed", order=300, icon="pa-servers",  
		description="If first gate keeper is passed, go ahead to check second gate keeper; otherwise, consider "
				+ "the whole gate keeper as passed.")
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
		} else if (ifResult instanceof PendingAndBlock) {
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
