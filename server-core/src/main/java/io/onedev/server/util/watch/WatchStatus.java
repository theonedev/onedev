package io.onedev.server.util.watch;

public enum WatchStatus {
	DEFAULT("Default"), 
	WATCH("Watch"), 
	IGNORE("Ignore");
	
	private final String displayName;
	
	WatchStatus(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
	
}