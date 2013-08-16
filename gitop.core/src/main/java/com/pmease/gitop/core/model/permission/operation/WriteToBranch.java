package com.pmease.gitop.core.model.permission.operation;

import com.pmease.commons.util.pattern.WildcardUtils;

public class WriteToBranch implements PrivilegedOperation {

	private String filePaths = "**";
	
	public String getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(String filePaths) {
		this.filePaths = filePaths;
	}

	@Override
	public boolean can(PrivilegedOperation operation) {
		if (operation instanceof WriteToBranch) {
			WriteToBranch writeToBranch = (WriteToBranch) operation;
			return WildcardUtils.matchPath(getFilePaths(), writeToBranch.getFilePaths());
		} else {
			return new Read().can(operation);
		}
	}

}
