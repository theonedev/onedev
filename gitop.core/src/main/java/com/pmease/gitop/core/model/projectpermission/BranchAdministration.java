package com.pmease.gitop.core.model.projectpermission;

public class BranchAdministration implements BranchOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof BranchOperation;
	}

}
