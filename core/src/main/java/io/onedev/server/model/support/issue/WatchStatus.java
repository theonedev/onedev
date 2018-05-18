package io.onedev.server.model.support.issue;

public enum WatchStatus {
	DEFAULT("Default"), 
	WATCH("Watch"), 
	DO_NOT_WATCH("Do not watch");
	
	private final String displayName;
	
	WatchStatus(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}