package io.onedev.server.web.page.layout;

import java.util.List;

import io.onedev.server.web.page.base.BasePage;

public interface UICustomization {
	
	Class<? extends BasePage> getHomePage();
	
	List<MainTab> getMainTabs();
	
}
