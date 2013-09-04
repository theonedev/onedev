package com.pmease.gitop.core.model.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.gitop.core.model.MergeRequest;

public class AndGateKeeper implements GateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		boolean pending = false;
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result == CheckResult.REJECT || result == CheckResult.PENDING_BLOCK)
				return result;
			else if (result == CheckResult.PENDING)
				pending = true;
		}
		
		if (pending)
			return CheckResult.PENDING;
		else
			return CheckResult.ACCEPT;
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
