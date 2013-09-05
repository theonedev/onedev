package com.pmease.gitop.core.permission.operation;

public class Administration implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return true;
	}

}
