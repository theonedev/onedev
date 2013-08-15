package com.pmease.gitop.core.model.permission.account;

import com.pmease.commons.util.pattern.WildcardUtils;

public class WriteToBranch implements BranchOperation {
	
	private String filePaths;
	
	public WriteToBranch(String filePaths) {
		this.filePaths = filePaths;
	}

	public String getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(String filePaths) {
		this.filePaths = filePaths;
	}

	@Override
	public boolean can(PrivilegedOperation operation) {
		if (operation instanceof ReadFromBranch) {
			return true;
		} else if (operation instanceof WriteToBranch) {
			WriteToBranch writeOperation = (WriteToBranch) operation;
			return WildcardUtils.matchPath(filePaths, writeOperation.getFilePaths());
		} else {
			return false;
		}
	}

}
