package com.pmease.gitop.core.model.permission.operation;

public class Read implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof Read;
	}

}
