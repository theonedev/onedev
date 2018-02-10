package com.turbodev.server.web.component.tabbable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

public class TabDivider extends Tab {

	private static final long serialVersionUID = 1L;

	@Override
	public Component render(String componentId) {
		getItem().add(AttributeModifier.append("class", "divider"));
		return new WebMarkupContainer(componentId);
	}

	@Override
	public boolean isSelected() {
		return false;
	}

}
