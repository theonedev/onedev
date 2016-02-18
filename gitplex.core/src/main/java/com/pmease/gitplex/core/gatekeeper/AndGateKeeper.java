package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;

@SuppressWarnings("serial")
@Editable(name="And Composition", icon="fa-object-group", order=100, 
		category=GateKeeper.CATEGORY_COMPOSITION, 
		description="This gate keeper will be passed if all contained gate keepers are passed.")
@Horizontal
public class AndGateKeeper extends AndOrGateKeeper {

	@Override
	protected CheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = checker.check(each);
			if (result instanceof Passed) {
				acceptReasons.addAll(result.getReasons());
			} else if (result instanceof Failed) {
				return result;
			} else if (result instanceof Blocking) {
				result.getReasons().addAll(pendingReasons);
				return result;
			} else if (result instanceof Pending) {
				pendingReasons.addAll(result.getReasons());
			}
		}
		
		if (!pendingReasons.isEmpty())
			return pending(pendingReasons);
		else
			return passed(acceptReasons);
	}

}
