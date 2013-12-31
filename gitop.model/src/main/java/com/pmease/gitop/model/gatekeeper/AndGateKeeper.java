package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.PendingAndBlock;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable(name="If All Contained Gate Keepers Are Passed", icon="icon-servers", order=100, 
		description="This gate keeper will be passed if all contained gate keepers are passed.")
@TableLayout
public class AndGateKeeper extends AndOrGateKeeper {

	@Override
	protected CheckResult aggregate(Checker checker) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = checker.check(each);
			if (result instanceof Accepted) {
				acceptReasons.addAll(result.getReasons());
			} else if (result instanceof Rejected) {
				return result;
			} else if (result instanceof PendingAndBlock) {
				result.getReasons().addAll(pendingReasons);
				result.getVoteEligibilities().addAll(voteEligibilities);
				return result;
			} else {
				pendingReasons.addAll(result.getReasons());
				voteEligibilities.addAll(result.getVoteEligibilities());
			}
		}
		
		if (!pendingReasons.isEmpty())
			return pending(pendingReasons, voteEligibilities);
		else
			return accepted(acceptReasons);
	}

}
