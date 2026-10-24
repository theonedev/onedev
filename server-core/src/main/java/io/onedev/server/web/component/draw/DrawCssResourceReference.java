package io.onedev.server.web.component.draw;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class DrawCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public DrawCssResourceReference() {
		super(DrawCssResourceReference.class, "draw.css");
	}

}
