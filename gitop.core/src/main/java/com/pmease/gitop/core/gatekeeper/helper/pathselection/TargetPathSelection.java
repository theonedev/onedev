package com.pmease.gitop.core.gatekeeper.helper.pathselection;

import java.io.Serializable;

import com.pmease.commons.editable.Editable;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@Editable
public interface TargetPathSelection extends Serializable {
	GateKeeper getGateKeeper();
}
