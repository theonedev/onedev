package com.pmease.gitplex.core.branchmatcher;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable(name="All Branches", order=300)
public class MatchAllLocalBranches implements LocalBranchMatcher {

	@Override
	public boolean matches(String branch) {
		return true;
	}

}
