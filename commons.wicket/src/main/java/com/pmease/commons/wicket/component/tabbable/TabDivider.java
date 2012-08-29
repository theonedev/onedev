package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;

public class TabDivider implements Tab {

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
