package com.pmease.commons.wicket.component.tabbable;

import java.io.Serializable;

import org.apache.wicket.markup.html.list.ListItem;

public interface Tab extends Serializable {
	
	void populate(ListItem<Tab> item, String componentId);
	
	boolean isActive(ListItem<Tab> item);
}
