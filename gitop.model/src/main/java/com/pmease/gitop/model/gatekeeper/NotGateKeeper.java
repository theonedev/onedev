package com.pmease.gitop.model.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;

@SuppressWarnings("serial")
@Editable(name="Rejected By Specified Condition", category=GateKeeper.CATEGORY_COMPOSITE, 
		order=400, icon="icon-servers",  
		description="This condition will be satisified if contained condition is not satisfied.")
public class NotGateKeeper extends AbstractGateKeeper {

	private GateKeeper gateKeeper = new DefaultGateKeeper();
	
	@Editable
	@Valid
	@NotNull
	public GateKeeper getGateKeeper() {
		return gateKeeper;
	}
	
	public void setGateKeeper(GateKeeper gateKeeper) {
		this.gateKeeper = gateKeeper;
	}
	
	@Override
	public CheckResult check(PullRequest request) {
		CheckResult result = getGateKeeper().check(request);
		
		if (result instanceof Accepted)
			return rejected(result.getReasons());
		else if (result instanceof Rejected)
			return accepted(result.getReasons());
		else
			return result;
	}

	@Override
	public Object trim(Object context) {
		return getGateKeeper().trim(context);
	}

}
