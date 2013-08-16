package com.pmease.gitop.core.model.permission;

import java.util.ArrayList;
import java.util.List;

import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.permission.object.ProtectedBranches;
import com.pmease.gitop.core.model.permission.object.ProtectedObject;
import com.pmease.gitop.core.model.permission.operation.PrivilegedOperation;

public class RepositoryOperation {
	
	private PrivilegedOperation repositoryLevel;
	
	private List<BranchPermission> branchLevel = new ArrayList<BranchPermission>();

	public PrivilegedOperation getRepositoryWide() {
		return repositoryLevel;
	}

	public void setRepositoryLevel(PrivilegedOperation repositoryLevel) {
		this.repositoryLevel = repositoryLevel;
	}

	public List<BranchPermission> getBranchLevel() {
		return branchLevel;
	}

	public void setBranchLevel(List<BranchPermission> branchLevel) {
		this.branchLevel = branchLevel;
	}

	public PrivilegedOperation operationOf(ProtectedObject object, Repository repository) {
		for (BranchPermission each: getBranchLevel()) {
			if (new ProtectedBranches(repository, each.getBranchNames()).has(object))
				return each.getBranchOperation();
		}
		
		if (repository.has(object))
			return getRepositoryWide();
		else
			return null;
	}

}
