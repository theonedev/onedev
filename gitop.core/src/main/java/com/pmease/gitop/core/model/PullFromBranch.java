package com.pmease.gitop.core.model;

public class PullFromBranch implements BranchOperation {

	@Override
	public boolean implies(BranchOperation branchOperation) {
		return branchOperation instanceof PullFromBranch;
	}

}
