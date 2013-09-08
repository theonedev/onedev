package com.pmease.gitop.core.permission.operation;

@SuppressWarnings("serial")
public class Write implements PrivilegedOperation{

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof Write || new Read().can(operation);
	}

}
