package com.pmease.gitop.core.model;

import com.pmease.commons.util.PathUtils;
import com.pmease.commons.util.StringUtils;

public class BranchPermission {

	private String repositoryNamePattern = "*";
	
	private String branchNamePattern = "**";
	
	private BranchOperation branchOperation = new PullFromBranch();
	
	private boolean allow;
	
	public String getRepositoryNamePattern() {
		return repositoryNamePattern;
	}

	public void setRepositoryNamePattern(String repositoryNamePattern) {
		this.repositoryNamePattern = repositoryNamePattern;
	}

	public String getBranchNamePattern() {
		return branchNamePattern;
	}

	public void setBranchNamePattern(String branchNamePattern) {
		this.branchNamePattern = branchNamePattern;
	}

	public BranchOperation getBranchOperation() {
		return branchOperation;
	}

	public void setBranchOperation(BranchOperation branchOperation) {
		this.branchOperation = branchOperation;
	}

	public boolean isAllow() {
		return allow;
	}

	public void setAllow(boolean allow) {
		this.allow = allow;
	}

	public Boolean permits(String repositoryName, String branchName, BranchOperation branchOperation) {
		if (StringUtils.wildcardMatch(getRepositoryNamePattern(), repositoryName) 
				&& PathUtils.match(getBranchNamePattern(), branchName)
				&& getBranchOperation().implies(branchOperation)) {
			return allow;
		} else {
			return null;
		}
	}
}
