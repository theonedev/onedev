package io.onedev.server.util.usage;

import io.onedev.commons.utils.ExplicitException;

public class InUseException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public InUseException(String message) {
		super(message);
	}

}
