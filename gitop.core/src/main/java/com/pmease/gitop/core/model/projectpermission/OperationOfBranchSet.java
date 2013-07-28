package com.pmease.gitop.core.model.projectpermission;

import com.pmease.commons.util.StringUtils;

public class OperationOfBranchSet implements RepositoryOperation {
	
	private String branchNames; 

	private BranchOperation branchOperation;

	public OperationOfBranchSet(String branchNames, BranchOperation branchOperation) {
		this.branchNames = branchNames;
		this.branchOperation = branchOperation;
	}
	
	public String getBranchNames() {
		return branchNames;
	}

	public void setBranchNames(String branchNames) {
		this.branchNames = branchNames;
	}

	public BranchOperation getBranchOperation() {
		return branchOperation;
	}

	public void setBranchOperation(BranchOperation branchOperation) {
		this.branchOperation = branchOperation;
	}

	@Override
	public boolean can(PrivilegedOperation operation) {
		if (operation instanceof OperationOfBranchSet) {
			OperationOfBranchSet operationOfBranchSet = (OperationOfBranchSet) operation;
			if (StringUtils.wildcardMatch(getBranchNames(), operationOfBranchSet.getBranchNames())) {
				return branchOperation.can(operationOfBranchSet.getBranchOperation());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
