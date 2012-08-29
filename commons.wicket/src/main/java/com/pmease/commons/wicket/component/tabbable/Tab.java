package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.markup.html.list.ListItem;

public interface Tab {
	
	void populate(ListItem<Tab> item, String componentId);
	
	boolean isActive(ListItem<Tab> item);
}
