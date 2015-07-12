package com.pmease.gitplex.core.branchmatcher;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
@Editable(name="All Branches from All Repositories", order=300)
public class MatchAllAffinalBranches implements AffinalBranchMatcher {

	@Override
	public Object trim(Object context) {
		return this;
	}

	@Override
	public boolean matches(Repository repository, String branch) {
		return true;
	}

}
