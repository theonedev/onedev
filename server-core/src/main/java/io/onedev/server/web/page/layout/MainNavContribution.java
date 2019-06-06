package io.onedev.server.web.page.layout;

public interface MainNavContribution {
	
	int getOrder();

	Class<? extends LayoutPage> getPageClass();
	
	String getLabel();
	
	boolean isAuthorized();
	
	boolean isActive(LayoutPage page);
}