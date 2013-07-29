package com.pmease.gitop.core.model.permission.account;

public class ReadFromRepository implements RepositoryOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof ReadFromRepository
				|| new OperationOfBranchSet("**", new ReadFromBranch()).can(operation);
	}

}
