package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable(name="Any sub gate keeper accepts")
public class OrGateKeeper extends AbstractGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();

	@Editable(name="Sub Gate Keepers")
	@NotNull
	@Size(min=1, message="At least one element has to be added.")
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		List<String> pendingReasons = new ArrayList<String>();
		List<String> rejectReasons = new ArrayList<String>();
		
		for (GateKeeper each: getGateKeepers()) {
			CheckResult result = each.check(request);
			if (result.isReject()) {
				rejectReasons.addAll(result.getReasons());
			} else if (result.isAccept()) {
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
			return reject(rejectReasons);
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
