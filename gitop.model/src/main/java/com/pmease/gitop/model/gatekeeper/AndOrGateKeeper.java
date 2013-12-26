package com.pmease.gitop.model.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.util.trimmable.AndOrConstruct;
import com.pmease.commons.util.trimmable.TrimUtils;
import com.pmease.gitop.model.Project;

@SuppressWarnings("serial")
public abstract class AndOrGateKeeper extends CompositeGateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Valid
	@NotNull
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	@Override
	protected GateKeeper trim(Project project) {
		return (GateKeeper) TrimUtils.trim(new AndOrConstruct() {
			
			@Override
			public Object getSelf() {
				return AndOrGateKeeper.this;
			}
			
			@Override
			public List<?> getMembers() {
				return getGateKeepers();
			}
			
		}, project);
	}

}
