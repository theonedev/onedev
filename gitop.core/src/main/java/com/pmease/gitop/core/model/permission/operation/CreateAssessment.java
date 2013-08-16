package com.pmease.gitop.core.model.permission.operation;

public class CreateAssessment implements PrivilegedOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof CreateAssessment;
	}

}
