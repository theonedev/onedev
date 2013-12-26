package com.pmease.gitop.web.common.wicket.component.tab;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface ITabEx extends ITab, IGroupable {
	
	String getTabId();
	
	boolean isSelected(ITabEx activeTab);
}
