package com.pmease.gitop.core.model.projectpermission;

import com.pmease.commons.util.PathUtils;

public class PushToBranch implements BranchOperation {
	
	private String filePaths;
	
	public PushToBranch(String filePaths) {
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
		if (operation instanceof PullFromBranch) {
			return true;
		} else if (operation instanceof PushToBranch) {
			PushToBranch pushOperation = (PushToBranch) operation;
			return PathUtils.match(filePaths, pushOperation.getFilePaths());
		} else {
			return false;
		}
	}

}
