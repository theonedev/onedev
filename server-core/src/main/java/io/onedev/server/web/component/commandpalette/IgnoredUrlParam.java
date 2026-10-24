package io.onedev.server.web.component.commandpalette;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;

public class IgnoredUrlParam extends ExplicitException {

	@Serial
	private static final long serialVersionUID = 1L;

	public IgnoredUrlParam(String message) {
		super(message);
	}

}
