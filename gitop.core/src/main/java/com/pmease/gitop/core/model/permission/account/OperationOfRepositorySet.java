package com.pmease.gitop.core.model.permission.account;

import com.pmease.commons.util.StringUtils;

public class OperationOfRepositorySet implements AccountOperation {

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
	
	public static OperationOfRepositorySet ofRepositoryAdmin(String repositoryName) {
		return new OperationOfRepositorySet(repositoryName, new RepositoryAdministration());
	}

	public static OperationOfRepositorySet ofRepositoryRead(String repositoryName) {
		return new OperationOfRepositorySet(repositoryName, new ReadFromRepository());
	}

	public static OperationOfRepositorySet ofRepositoryWrite(String repositoryName) {
		return new OperationOfRepositorySet(repositoryName, new WriteToRepository());
	}

	public static OperationOfRepositorySet ofBranchAdmin(String repositoryName, String branchName) {
		return new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new BranchAdministration()));
	}

	public static OperationOfRepositorySet ofBranchRead(String repositoryName, String branchName) {
		return new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new ReadFromBranch()));
	}

	public static OperationOfRepositorySet ofBranchWrite(String repositoryName, String branchName) {
		return new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new WriteToBranch("**")));
	}

	public static OperationOfRepositorySet ofBranchWrite(String repositoryName, String branchName, String filePath) {
		return new OperationOfRepositorySet(repositoryName, new OperationOfBranchSet(branchName, new WriteToBranch(filePath)));
	}

}
