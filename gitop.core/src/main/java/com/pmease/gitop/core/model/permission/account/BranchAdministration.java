package com.pmease.gitop.core.model.permission.account;

public class BranchAdministration implements BranchOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof BranchOperation;
	}

}
