package com.pmease.gitop.core.model.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public class AndGateKeeper implements GateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	@Size(min=2)
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Override
	public CheckResult check(MergeRequest mergeRequest) {
		boolean undetermined = false;
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(mergeRequest);
			if (result == CheckResult.REJECT)
				return CheckResult.REJECT;
			else if (result == CheckResult.UNDETERMINED)
				undetermined = true;
		}
		
		if (undetermined)
			return CheckResult.UNDETERMINED;
		else
			return CheckResult.ACCEPT;
	}

	@Override
	public Trimmable trim() {
		return TrimUtils.trim(new AndOrConstruct() {
			
			@Override
			public Trimmable getSelf() {
				return AndGateKeeper.this;
			}
			
			@Override
			public List<? extends Trimmable> getMembers() {
				return getGateKeepers();
			}
		});
	}

}
