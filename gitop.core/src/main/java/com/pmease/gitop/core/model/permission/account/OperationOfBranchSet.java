package com.pmease.gitop.core.model.permission.account;

import com.pmease.commons.util.pattern.WildcardUtils;

public class OperationOfBranchSet implements RepositoryOperation {
	
	private String branchPatterns; 

	private BranchOperation branchOperation;

	public OperationOfBranchSet(String branchPatterns, BranchOperation branchOperation) {
		this.branchPatterns = branchPatterns;
		this.branchOperation = branchOperation;
	}
	
	public String getBranchPatterns() {
		return branchPatterns;
	}

	public void setBranchPatterns(String branchPatterns) {
		this.branchPatterns = branchPatterns;
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
			if (WildcardUtils.matchString(getBranchPatterns(), operationOfBranchSet.getBranchPatterns())) {
				return branchOperation.can(operationOfBranchSet.getBranchOperation());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
