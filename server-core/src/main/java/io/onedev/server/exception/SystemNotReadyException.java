package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class SystemNotReadyException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public SystemNotReadyException() {
		super("System not ready");
	}

}
