package com.pmease.gitop.core.gatekeeper.helper.branchselection;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.BranchChoice;
import com.pmease.gitop.core.gatekeeper.IfSubmitToSpecifiedBranches;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class SpecifyTargetBranchesByIds implements TargetBranchSelection {

	private List<Long> branchIds = new ArrayList<>();
	
	@Editable(name="Branch Names")
	@BranchChoice
	@OmitName
	@NotNull
	@Size(min=1)
	public List<Long> getBranchIds() {
		return branchIds;
	}

	public void setBranchIds(List<Long> branchIds) {
		this.branchIds = branchIds;
	}

	@Override
	public GateKeeper getGateKeeper() {
		IfSubmitToSpecifiedBranches gateKeeper = new IfSubmitToSpecifiedBranches(); 
		gateKeeper.getBranchIds().addAll(branchIds);
		return gateKeeper;
	}

	@Override
	public Object trim(Object context) {
		Gitop.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

}
