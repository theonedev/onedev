package com.pmease.gitplex.core.util.branchmatcher;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.model.Depot;

@SuppressWarnings("serial")
@Editable(name="All Branches from All Repositories", order=300)
public class MatchAllAffinalBranches implements AffinalBranchMatcher {

	@Override
	public Object trim(Object context) {
		return this;
	}

	@Override
	public boolean matches(Depot depot, String branch) {
		return true;
	}

}
