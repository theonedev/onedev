package com.pmease.gitop.core.model.permission.account;

public class ReadFromAccount implements AccountWideOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof ReadFromAccount 
				|| new OperationOfRepositorySet("*", new ReadFromRepository()).can(operation);
	}

}
