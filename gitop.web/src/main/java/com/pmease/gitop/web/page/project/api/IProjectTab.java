package com.pmease.gitop.web.page.project.api;

import com.pmease.gitop.web.common.wicket.component.tab.IPageTab;

public interface IProjectTab extends IPageTab {
	
	public static enum Category {
		CODE,
		WIKI,
		ISSUES,
		STATISTICS
	}
}
