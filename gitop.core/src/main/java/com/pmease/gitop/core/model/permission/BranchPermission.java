package com.pmease.gitop.core.model.permission;

import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;

public class BranchPermission {
	
	private String branchNames;
	
	private PrivilegedOperation branchOperation;

	public String getBranchNames() {
		return branchNames;
	}

	public void setBranchNames(String branchNames) {
		this.branchNames = branchNames;
	}

	public PrivilegedOperation getBranchOperation() {
		return branchOperation;
	}

	public void setBranchOperation(PrivilegedOperation branchOperation) {
		this.branchOperation = branchOperation;
	}

}
