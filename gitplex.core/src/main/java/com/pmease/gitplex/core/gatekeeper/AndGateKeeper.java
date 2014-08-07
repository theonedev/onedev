package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitplex.core.gatekeeper.checkresult.Approved;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Disapproved;
import com.pmease.gitplex.core.gatekeeper.checkresult.Pending;
import com.pmease.gitplex.core.gatekeeper.checkresult.PendingAndBlock;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable(name="If All Contained Gate Keepers Are Passed", icon="fa-servers", order=100, 
		description="This gate keeper will be passed if all contained gate keepers are passed.")
@Horizontal
public class AndGateKeeper extends AndOrGateKeeper {

	@Override
	protected CheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = checker.check(each);
			if (result instanceof Approved) {
				acceptReasons.addAll(result.getReasons());
			} else if (result instanceof Disapproved) {
				return result;
			} else if (result instanceof PendingAndBlock) {
				result.getReasons().addAll(pendingReasons);
				result.getVoteEligibilities().addAll(voteEligibilities);
				return result;
			} else if (result instanceof Pending) {
				pendingReasons.addAll(result.getReasons());
				voteEligibilities.addAll(result.getVoteEligibilities());
			}
		}
		
		if (!pendingReasons.isEmpty())
			return pending(pendingReasons, voteEligibilities);
		else
			return approved(acceptReasons);
	}

}
