package com.pmease.gitop.core.model.permission.account;

public class ReadFromBranch implements BranchOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof ReadFromBranch;
	}

}
