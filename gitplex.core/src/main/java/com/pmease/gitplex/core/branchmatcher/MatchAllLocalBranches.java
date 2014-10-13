package com.pmease.gitplex.core.branchmatcher;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.model.Branch;

@SuppressWarnings("serial")
@Editable(name="All Branches", order=300)
public class MatchAllLocalBranches implements LocalBranchMatcher {

	@Override
	public boolean matches(Branch branch) {
		return true;
	}

	@Override
	public Object trim(Object context) {
		return this;
	}

}
