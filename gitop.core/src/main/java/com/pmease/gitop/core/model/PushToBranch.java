package com.pmease.gitop.core.model;

import com.pmease.commons.util.PathUtils;

public class PushToBranch implements BranchOperation {

	private String filePath = "**";

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public boolean implies(BranchOperation branchOperation) {
		if (branchOperation instanceof PushToBranch) {
			PushToBranch pushToBranch = (PushToBranch) branchOperation;
			return PathUtils.match(getFilePath(), pushToBranch.getFilePath());
		} else {
			return false;
		}
	}

}
