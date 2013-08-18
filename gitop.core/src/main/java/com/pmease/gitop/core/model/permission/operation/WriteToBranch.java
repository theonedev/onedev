package com.pmease.gitop.core.model.permission.operation;

import javax.annotation.Nullable;

import com.pmease.commons.util.pattern.WildcardUtils;

public class WriteToBranch implements PrivilegedOperation {

	private final String filePaths;
	
	/**
	 * Construct branch write operation with specified file paths.
	 * <p>
	 * @param filePaths
	 * 			null to indicate the operation will not touch any files
	 * 			
	 */
	public WriteToBranch(@Nullable String filePaths) {
		this.filePaths = filePaths;
	}
	
	public String getFilePaths() {
		return filePaths;
	}

	@Override
	public boolean can(PrivilegedOperation operation) {
		if (operation instanceof WriteToBranch) {
			WriteToBranch writeToBranch = (WriteToBranch) operation;
			if (writeToBranch.getFilePaths() == null)
				return true;
			else if (getFilePaths() == null)
				return false;
			else
				return WildcardUtils.matchPath(getFilePaths(), writeToBranch.getFilePaths());
		} else {
			return new Read().can(operation);
		}
	}

}
