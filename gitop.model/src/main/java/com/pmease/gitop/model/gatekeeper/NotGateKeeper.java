package com.pmease.gitop.model.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Editable(name="If Contained Gate Keeper Is Not Passed", order=400, icon="icon-servers",  
		description="This gate keeper will be passed if contained gate keeper is not passed.")
@TableLayout
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
	protected GateKeeper trim(Project project) {
		return (GateKeeper) getGateKeeper().trim(project);
	}

	@Override
	protected CheckResult aggregate(Checker checker) {
		CheckResult result = checker.check(getGateKeeper());
		
		if (result instanceof Accepted)
			return rejected(result.getReasons());
		else if (result instanceof Rejected)
			return accepted(result.getReasons());
		else
			return result;
	}

}
