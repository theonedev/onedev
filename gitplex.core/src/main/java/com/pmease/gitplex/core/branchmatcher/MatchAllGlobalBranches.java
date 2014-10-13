package com.pmease.gitplex.core.branchmatcher;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.model.Branch;

@SuppressWarnings("serial")
@Editable(name="All Branches from All Repositories", order=300)
public class MatchAllGlobalBranches implements GlobalBranchMatcher {

	@Override
	public Object trim(Object context) {
		return this;
	}

	@Override
	public boolean matches(Branch branch) {
		return true;
	}

}
