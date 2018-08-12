package io.onedev.server.web.page.layout;

public interface LayoutMenuContribution {
	
	int getOrder();

	Class<? extends LayoutPage> getPageClass();
	
	String getLabel();
	
	boolean isAuthorized();
}