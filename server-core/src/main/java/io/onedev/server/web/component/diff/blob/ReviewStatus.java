package io.onedev.server.web.component.diff.blob;

public interface ReviewStatus {
	
	boolean isCollapsed();
	
	void setCollapsed(boolean collapsed);
	
	boolean isReviewed();
	
	void setReviewed(boolean reviewed);
	
}
