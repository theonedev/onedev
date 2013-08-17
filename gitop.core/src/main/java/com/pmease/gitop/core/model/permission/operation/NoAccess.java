package com.pmease.gitop.core.model.permission.operation;

public class NoAccess implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return false;
	}

}
