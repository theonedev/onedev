package com.pmease.gitop.core.model.projectpermission;

import com.pmease.commons.util.StringUtils;

public class OperationOfRepositorySet implements PrivilegedOperation {

	private String repositoryNames;
	
	private RepositoryOperation repositoryOperation;
	
	public OperationOfRepositorySet(String repositoryNames, RepositoryOperation repositoryOperation) {
		this.repositoryNames = repositoryNames;
		this.repositoryOperation = repositoryOperation;
	}
	
	public String getRepositoryNames() {
		return repositoryNames;
	}

	public void setRepositoryNames(String repositoryNames) {
		this.repositoryNames = repositoryNames;
	}

	public RepositoryOperation getRepositoryOperation() {
		return repositoryOperation;
	}

	public void setRepositoryPermission(RepositoryOperation repositoryOperation) {
		this.repositoryOperation = repositoryOperation;
	}

	@Override
	public boolean can(PrivilegedOperation operation) {
		if (operation instanceof OperationOfRepositorySet) {
			OperationOfRepositorySet operationOfRepositorySet = (OperationOfRepositorySet) operation;
			if (StringUtils.wildcardMatch(getRepositoryNames(), operationOfRepositorySet.getRepositoryNames())) {
				return getRepositoryOperation().can(operationOfRepositorySet.getRepositoryOperation());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
