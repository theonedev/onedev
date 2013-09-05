package com.pmease.gitop.core.permission.operation;

public class NoAccess implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return false;
	}

}
