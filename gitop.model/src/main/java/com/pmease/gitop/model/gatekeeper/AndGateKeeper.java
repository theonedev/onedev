package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable(name="All Of Contained Conditions Are Satisfied", category=GateKeeper.CATEGORY_COMPOSITE, 
		icon="icon-servers", order=100, 
		description="This condition will be satisified if all of its contained conditions are satisfied.")
public class AndGateKeeper extends AbstractGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Editable(name="Sub Gate Keepers")
	@Valid
	@NotNull
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	public CheckResult check(PullRequest request) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result instanceof Accepted) {
				acceptReasons.addAll(result.getReasons());
			} else if (result instanceof Rejected) {
				return result;
			} else if (result instanceof Blocked) {
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

	@Override
	public Object trim(Object context) {
		return TrimUtils.trim(new AndOrConstruct() {
			
			@Override
			public Object getSelf() {
				return AndGateKeeper.this;
			}
			
			@Override
			public List<?> getMembers() {
				return getGateKeepers();
			}
			
		}, context);
	}

}
