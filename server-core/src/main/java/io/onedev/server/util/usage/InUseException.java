package io.onedev.server.util.usage;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;

public class InUseException extends ExplicitException {

	@Serial
	private static final long serialVersionUID = 1L;

	public InUseException(String message) {
		super(message);
	}

}
