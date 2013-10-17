package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable(name="All Of Below Sub Gate Keepers Accept")
@TableLayout
public class AndGateKeeper extends AbstractGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Editable(name="Sub Gate Keepers")
	@Valid
	@NotNull
	@Size(min=1, message="At least one element has to be added.")
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
