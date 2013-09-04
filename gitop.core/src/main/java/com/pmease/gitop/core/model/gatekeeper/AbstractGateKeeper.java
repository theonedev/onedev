package com.pmease.gitop.core.model.gatekeeper;

import javax.annotation.Nullable;

public abstract class AbstractGateKeeper implements GateKeeper {

	@Override
	public Object trim(@Nullable Object context) {
		return this;
	}

}
