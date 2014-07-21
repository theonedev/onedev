package com.pmease.gitplex.web.common.wicket.component.tab;

import org.apache.wicket.Page;

public interface IPageTab extends ITabEx {
	
	boolean isSelected(Page page);
	
}
