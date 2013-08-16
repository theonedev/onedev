package com.pmease.gitop.core.model.permission.operation;

public class CreateComment implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof CreateComment;
	}

}
