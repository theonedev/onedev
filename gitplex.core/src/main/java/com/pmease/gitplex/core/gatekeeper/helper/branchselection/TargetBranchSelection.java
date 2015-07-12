package com.pmease.gitplex.core.gatekeeper.helper.branchselection;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;

@Editable
public interface TargetBranchSelection extends Serializable {
	
	GateKeeper getGateKeeper();
}
