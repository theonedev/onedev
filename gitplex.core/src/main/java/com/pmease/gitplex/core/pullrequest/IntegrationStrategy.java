package com.pmease.gitplex.core.pullrequest;

public enum IntegrationStrategy {
	MERGE_ALWAYS("Merge (always create merge commit)"), 
	MERGE_IF_NECESSARY("Merge (create merge commit if diverged)"),
	REBASE_SOURCE("Rebase source and fast forward target"), 
	REBASE_TARGET("Rebase target on top of source");
	
	private String displayName;

	IntegrationStrategy(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
