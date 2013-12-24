package com.pmease.commons.wicket.behavior.menu;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;

import com.pmease.commons.wicket.asset.CommonHeaderItem;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;

@SuppressWarnings("serial")
public class MenuBehavior extends DropdownBehavior {

	public MenuBehavior(MenuPanel menuPanel) {
		super(menuPanel);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		response.render(CommonHeaderItem.get());
	}
	
}
