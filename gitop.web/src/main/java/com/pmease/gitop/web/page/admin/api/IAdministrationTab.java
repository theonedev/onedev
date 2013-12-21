package com.pmease.gitop.web.page.admin.api;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface IAdministrationTab extends ITab {
	public static enum Category {
		ACCOUNTS,
		SETTINGS,
		SUPPORT
	}
	
	Category getCategory();
	
	String getTabId();
	
	Component newTabLink(String id);
}
