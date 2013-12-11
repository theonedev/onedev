package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.model.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable(name="Any Of Contained Conditions Are Satisfied", category=GateKeeper.CATEGORY_COMPOSITE, order=200, 
		description="This condition will be satisfied if any of the contained conditions is satisfied.")
public class OrGateKeeper extends AbstractGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();

	@Editable(name="Sub Gate Keepers")
	@Valid
	@NotNull
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Override
	public CheckResult check(PullRequest request) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> rejectReasons = new ArrayList<String>();
		Collection<VoteEligibility> voteEligibilities = new ArrayList<>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result instanceof Rejected) {
				rejectReasons.addAll(result.getReasons());
			} else if (result instanceof Accepted) {
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
			return rejected(rejectReasons);
	}

	@Override
	public Object trim(Object context) {
		return TrimUtils.trim(new AndOrConstruct() {
			
			@Override
			public Trimmable getSelf() {
				return OrGateKeeper.this;
			}
			
			@Override
			public List<? extends Trimmable> getMembers() {
				return getGateKeepers();
			}
			
		}, context);
	}

}
