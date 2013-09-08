package com.pmease.gitop.core.permission.operation;

@SuppressWarnings("serial")
public class CreateMergeRequest implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof CreateMergeRequest;
	}

}
