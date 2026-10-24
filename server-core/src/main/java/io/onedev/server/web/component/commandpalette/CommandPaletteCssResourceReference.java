package io.onedev.server.web.component.commandpalette;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CommandPaletteCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CommandPaletteCssResourceReference() {
		super(CommandPaletteCssResourceReference.class, "command-palette.css");
	}

}
