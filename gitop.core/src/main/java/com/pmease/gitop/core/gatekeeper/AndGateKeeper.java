package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
public class AndGateKeeper extends AbstractGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> acceptReasons = new ArrayList<String>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result.isAccept()) {
				acceptReasons.addAll(result.getReasons());
			} else if (result.isReject()) {
				return result;
			} else if (result.isBlock()) {
				result.getReasons().addAll(pendingReasons);
				return result;
			} else {
				pendingReasons.addAll(result.getReasons());
			}
		}
		
		if (!pendingReasons.isEmpty())
			return pending(pendingReasons);
		else
			return accept(acceptReasons);
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
