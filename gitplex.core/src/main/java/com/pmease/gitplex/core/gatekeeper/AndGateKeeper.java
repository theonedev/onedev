package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Blocking;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Failed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;

@Editable(name="And Composition", icon="fa-object-group", order=100, 
		category=GateKeeper.CATEGORY_COMPOSITION, 
		description="This gatekeeper will be passed if all contained child gatekeepers are passed")
@Horizontal
public class AndGateKeeper extends AndOrGateKeeper {

	private static final long serialVersionUID = 1L;

	@Override
	protected GateCheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		for (GateKeeper each: getGateKeepers()) {
			GateCheckResult result = checker.check(each);
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
