package com.pmease.gitop.core.extensions.gatekeeper.helper.branchselection;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@Editable
public interface TargetBranchSelection extends Trimmable, Serializable {
	
	GateKeeper getGateKeeper();
}
