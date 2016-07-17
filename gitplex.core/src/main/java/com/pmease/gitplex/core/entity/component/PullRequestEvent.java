package com.pmease.gitplex.core.entity.component;

public enum PullRequestEvent {
	OPENED("opened"), 
	ASSIGNED("assigned"),
	APPROVED("approved"), 
	DISAPPROVED("disapproved"),
	REVIEW_WITHDRAWED("review withdrawed"),
	UPDATED("has new commits"), 
	COMMENTED("commented"), 
	INTEGRATED("integrated"), 
	DISCARDED("discarded"), 
	REOPENED("reopened"),
	CODE_COMMENTED("code commented"),
	CODE_COMMENT_REPLIED("code comment replied"),
	CODE_COMMENT_RESOLVED("code comment resolved"),
	CODE_COMMENT_UNRESOLVED("code comment unresolved"),
	SOURCE_BRANCH_RESTORED("source branch restored"),
	SOURCE_BRANCH_DELETED("source branch deleted");
	
	private final String displayName;
	
	PullRequestEvent(String displayName) {
		this.displayName = displayName;
	}
	
	public String toString() {
		return displayName;
	}	
	
}
