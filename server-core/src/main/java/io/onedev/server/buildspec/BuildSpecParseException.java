package io.onedev.server.buildspec;

import io.onedev.commons.utils.ExplicitException;

public class BuildSpecParseException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public BuildSpecParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
