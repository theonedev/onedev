package com.pmease.gitop.core.permission.operation;

public class CreateRepository implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof CreateRepository;
	}

}
