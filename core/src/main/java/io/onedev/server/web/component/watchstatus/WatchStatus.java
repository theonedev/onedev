package io.onedev.server.web.component.watchstatus;

public enum WatchStatus {
	WATCHING("Watching"), 
	NOT_WATCHING("Not watching"), 
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