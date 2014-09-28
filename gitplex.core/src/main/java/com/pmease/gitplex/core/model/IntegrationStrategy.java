package com.pmease.gitplex.core.model;

public enum IntegrationStrategy {
	MERGE_ALWAYS("Merge always"), 
	MERGE_IF_NECESSARY("Merge if necessary"),
	MERGE_WITH_SQUASH("Merge with squash"),
	REBASE_SOURCE("Rebase source on top of target"), 
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
