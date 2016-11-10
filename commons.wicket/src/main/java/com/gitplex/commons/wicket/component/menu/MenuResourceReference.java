package com.gitplex.commons.wicket.component.menu;

import org.apache.wicket.request.resource.CssResourceReference;

public class MenuResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public MenuResourceReference() {
		super(MenuResourceReference.class, "menu.css");
	}

}
