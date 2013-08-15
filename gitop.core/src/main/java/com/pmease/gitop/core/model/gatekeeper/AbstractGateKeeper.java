package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.commons.util.trimmable.Trimmable;

public abstract class AbstractGateKeeper implements GateKeeper {

	@Override
	public Trimmable trim() {
		return this;
	}

}
