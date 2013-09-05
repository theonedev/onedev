package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public class OrGateKeeper implements GateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		boolean pending = false;
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result == CheckResult.ACCEPT || result == CheckResult.PENDING_BLOCK)
				return result;
			else if (result == CheckResult.PENDING)
				pending = true;
		}
		
		if (pending)
			return CheckResult.PENDING;
		else
			return CheckResult.REJECT;
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
