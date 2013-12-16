package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;

@SuppressWarnings("serial")
public abstract class AndOrGateKeeper extends CompositeGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	public Object trim(Object context) {
		return TrimUtils.trim(new AndOrConstruct() {
			
			@Override
			public Object getSelf() {
				return AndOrGateKeeper.this;
			}
			
			@Override
			public List<?> getMembers() {
				return getGateKeepers();
			}
			
		}, context);
	}

}
