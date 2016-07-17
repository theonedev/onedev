package com.pmease.gitplex.core.entity.component;

public enum CodeCommentEvent {
	CREATED("created"),
	REPLIED("replied"),
	RESOLVED("resolved"),
	UNRESOLVED("unresolved");
	
	private final String displayName;
	
	CodeCommentEvent(String displayName) {
		this.displayName = displayName;
	}
	
	public String toString() {
		return displayName;
	}	
	
}
