package com.pmease.gitop.core.model.permission.account;

public class WriteToRepository implements RepositoryOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof WriteToRepository
				|| new ReadFromRepository().can(operation)
				|| new OperationOfBranchSet("**", new WriteToBranch("**")).can(operation); 
	}

}
