package io.onedev.server.util.usage;

import io.onedev.server.OneException;

public class InUseException extends OneException {

	private static final long serialVersionUID = 1L;

	public InUseException(String message) {
		super(message);
	}

}
