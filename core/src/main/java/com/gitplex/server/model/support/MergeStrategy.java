package com.gitplex.server.model.support;

public enum MergeStrategy {
	ALWAYS_MERGE("Always Merge", 
			"Add all commits from source branch to target branch with a merge commit."), 
	MERGE_IF_NECESSARY("Merge If Necessary", 
			"Only create merge commit if target branch can not be fast-forwarded to source branch"),
	SQUASH_MERGE("Squash Merge", 
			"Squash all commits from source branch into a single commit in target branch"),
	REBASE_MERGE("Rebase Merge", 
			"Rebase all commits from source branch onto target branch"), 
	DO_NOT_MERGE("Do Not Merge", 
			"Do not merge now, only for review"); 

	private final String displayName;
	
	private final String description;
	
	MergeStrategy(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
}