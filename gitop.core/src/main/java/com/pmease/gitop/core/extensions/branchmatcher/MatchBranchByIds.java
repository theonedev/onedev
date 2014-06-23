package com.pmease.gitop.core.extensions.branchmatcher;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.editable.BranchChoice;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class MatchBranchByIds implements BranchMatcher {

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
		Gitop.getInstance(BranchManager.class).trim(branchIds);
		if (branchIds.isEmpty())
			return null;
		else
			return this;
	}

}
