package com.pmease.gitop.core.model.projectpermission;

public class PullFromProject implements WholeProjectOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof PullFromProject 
				|| new OperationOfRepositorySet("*", new OperationOfBranchSet("**", new PullFromBranch())).can(operation);
	}

}
