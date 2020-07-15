package io.onedev.server.util.usage;

import io.onedev.server.GeneralException;

public class InUseException extends GeneralException {

	private static final long serialVersionUID = 1L;

	public InUseException(String message) {
		super(message);
	}

}
