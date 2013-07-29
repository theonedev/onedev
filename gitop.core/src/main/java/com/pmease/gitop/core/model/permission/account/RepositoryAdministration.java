package com.pmease.gitop.core.model.permission.account;

public class RepositoryAdministration implements RepositoryOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof RepositoryOperation;
	}

}
