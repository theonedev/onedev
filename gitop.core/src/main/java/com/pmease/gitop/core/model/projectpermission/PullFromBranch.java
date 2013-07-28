package com.pmease.gitop.core.model.projectpermission;

public class PullFromBranch implements BranchOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof PullFromBranch;
	}

}
