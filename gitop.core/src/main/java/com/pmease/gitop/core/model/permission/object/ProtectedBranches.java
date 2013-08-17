package com.pmease.gitop.core.model.permission.object;

import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.User;

public class ProtectedBranches implements RepositoryBelonging {

	private final Repository repository;
	
	private final String branchNames;
	
	public ProtectedBranches(Repository repository, String branchNames) {
		this.repository = repository;
		this.branchNames = branchNames;
	}
	
	public String getBranchNames() {
		return branchNames;
	}
	
	@Override
	public boolean has(ProtectedObject object) {
		if (object instanceof ProtectedBranches) {
			ProtectedBranches branches = (ProtectedBranches) object;
			return getRepository().getId().equals(branches.getRepository().getId()) 
					&& WildcardUtils.matchPath(getBranchNames(), branches.getBranchNames());
		} else {
			return false;
		}
	}

	@Override
	public Repository getRepository() {
		return repository;
	}

	@Override
	public User getUser() {
		return getRepository().getUser();
	}
	
}
