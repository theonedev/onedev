package com.pmease.gitop.core.model.permission;

import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;

public class BranchPermission {
	
	private final String branchNames;
	
	private final PrivilegedOperation branchOperation;
	
	public BranchPermission(String branchNames, PrivilegedOperation branchOperation) {
		this.branchNames = branchNames;
		this.branchOperation = branchOperation;
	}

	public String getBranchNames() {
		return branchNames;
	}

	public PrivilegedOperation getBranchOperation() {
		return branchOperation;
	}

}
