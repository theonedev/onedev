package com.pmease.gitop.core.model.permission.operation;

import com.pmease.commons.util.pattern.WildcardUtils;

public class WriteToBranch implements PrivilegedOperation {

	private final String filePaths;
	
	public WriteToBranch(String filePaths) {
		this.filePaths = filePaths;
	}
	
	public String getFilePaths() {
		return filePaths;
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
