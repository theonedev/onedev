package io.onedev.server.exception;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;

public class ServerNotReadyException extends ExplicitException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ServerNotReadyException() {
		super("Server not ready");
	}

}
