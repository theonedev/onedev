package com.pmease.gitplex.core.util.branchmatcher;

import com.pmease.commons.wicket.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable(name="All Branches", order=300)
public class MatchAllLocalBranches implements LocalBranchMatcher {

	@Override
	public boolean matches(String branch) {
		return true;
	}

}
