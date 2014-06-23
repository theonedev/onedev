package com.pmease.gitop.core.extensions.gatekeeper.helper.pathselection;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@Editable
public interface TargetPathSelection extends Serializable {
	GateKeeper getGateKeeper();
}
