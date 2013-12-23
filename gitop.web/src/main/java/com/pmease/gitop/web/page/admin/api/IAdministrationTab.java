package com.pmease.gitop.web.page.admin.api;

import org.apache.wicket.Page;

import com.pmease.gitop.web.common.wicket.component.tab.ITabEx;

public interface IAdministrationTab extends ITabEx {
	public static enum Category {
		ACCOUNTS,
		SETTINGS,
		SUPPORT
	}
	
	boolean isSelected(Page page);
}
