package com.pmease.gitop.core.gatekeeper;

import javax.annotation.Nullable;

public abstract class AbstractGateKeeper implements GateKeeper {

	@Override
	public Object trim(@Nullable Object context) {
		return this;
	}

}
