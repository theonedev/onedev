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
@Editable(name="If Any Of Contained Gate Keepers Is Passed", order=200, icon="fa-servers",  
		description="This gate keeper will be passed if any of the contained gate keepers is passed.")
@Horizontal
public class OrGateKeeper extends AndOrGateKeeper {

	@Override
	protected CheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> rejectReasons = new ArrayList<String>();
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = checker.check(each);
			if (result instanceof Disapproved) {
				rejectReasons.addAll(result.getReasons());
			} else if (result instanceof Approved) {
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
			return disapproved(rejectReasons);
	}

}
