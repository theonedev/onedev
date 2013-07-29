package com.pmease.gitop.core.model.permission.account;

public class WriteToAccount implements AccountWideOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof WriteToAccount 
				|| new ReadFromAccount().can(operation)
				|| new OperationOfRepositorySet("*", new WriteToRepository()).can(operation); 
	}

}
