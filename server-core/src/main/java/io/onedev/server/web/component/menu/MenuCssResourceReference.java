package io.onedev.server.web.component.menu;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class MenuCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MenuCssResourceReference() {
		super(MenuCssResourceReference.class, "menu.css");
	}

}
