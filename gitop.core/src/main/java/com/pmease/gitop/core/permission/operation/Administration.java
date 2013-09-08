package com.pmease.gitop.core.permission.operation;

@SuppressWarnings("serial")
public class Administration implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return true;
	}

}
