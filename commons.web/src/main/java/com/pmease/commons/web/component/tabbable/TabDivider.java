package com.pmease.commons.web.component.tabbable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;

public class TabDivider implements Tab {

	private static final long serialVersionUID = 1L;

	@Override
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(AttributeModifier.append("class", "divider"));
		item.add(new WebMarkupContainer(componentId));
	}

	@Override
	public boolean isActive(ListItem<Tab> item) {
		return false;
	}

}
