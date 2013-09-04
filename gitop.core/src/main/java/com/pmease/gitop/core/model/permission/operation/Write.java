package com.pmease.gitop.core.model.permission.operation;

public class Write implements PrivilegedOperation{

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof Write || new Read().can(operation);
	}

}
