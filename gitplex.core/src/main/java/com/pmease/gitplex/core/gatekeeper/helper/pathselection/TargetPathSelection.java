package com.pmease.gitplex.core.gatekeeper.helper.pathselection;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;

@Editable
public interface TargetPathSelection extends Serializable {
	GateKeeper getGateKeeper();
}
