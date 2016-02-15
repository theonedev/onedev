package com.pmease.gitplex.core.model;

import com.pmease.commons.git.GitUtils;

public class DepotAndBranch extends DepotAndRevision {

	private static final long serialVersionUID = 1L;
	
	public DepotAndBranch(Long depotId, String branch) {
		super(depotId, branch);
	}

	public DepotAndBranch(Depot depot, String branch) {
		super(depot, branch);
	}

	public DepotAndBranch(String depotAndBranch) {
		super(depotAndBranch);
	}
	
	@Override
	public String getBranch() {
		return getRevision();
	}

	@Override
	protected String normalizeRevision() {
		return GitUtils.branch2ref(getBranch());
	}
	
}
