package com.pmease.commons.web.behavior.menu;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.web.behavior.dropdown.DropdownBehavior;

@SuppressWarnings("serial")
public class MenuBehavior extends DropdownBehavior {

	public MenuBehavior(MenuPanel menuPanel) {
		super(menuPanel);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(MenuBehavior.class, "menu.css")));
	}
	
}
