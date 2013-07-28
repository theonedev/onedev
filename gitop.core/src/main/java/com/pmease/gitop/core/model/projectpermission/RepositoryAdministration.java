package com.pmease.gitop.core.model.projectpermission;

public class RepositoryAdministration implements RepositoryOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof RepositoryOperation;
	}

}
