package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class NotReadyException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public NotReadyException() {
		super("System is not ready");
	}

}
