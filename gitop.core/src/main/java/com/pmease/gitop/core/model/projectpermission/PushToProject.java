package com.pmease.gitop.core.model.projectpermission;

public class PushToProject implements WholeProjectOperation {

	@Override
	public boolean can(PrivilegedOperation operation) {
		return operation instanceof PushToProject 
				|| new PullFromProject().can(operation)
				|| new OperationOfRepositorySet("*", new OperationOfBranchSet("**", new PushToBranch("**"))).can(operation); 
	}

}
