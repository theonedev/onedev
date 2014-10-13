package com.pmease.gitplex.core.branchmatcher;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.model.Branch;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class MatchLocalBranchesByIds implements LocalBranchMatcher {

	private List<Long> branchIds = new ArrayList<>();
	
	@Editable(name="Branch Names")
	@BranchChoice
	@OmitName
	@NotNull
	@Size(min=1, message="At least one branch has to be selected.")
	public List<Long> getBranchIds() {
		return branchIds;
	}

	public void setBranchIds(List<Long> branchIds) {
		this.branchIds = branchIds;
	}

	@Override
	public boolean matches(Branch branch) {
		for (Long branchId: getBranchIds()) {
			if (branchId.equals(branch.getId()))
				return true;
		}
		return false;
	}

	@Override
	public Object trim(Object context) {
		GitPlex.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

}
