package io.onedev.server.git.exception;

import java.io.Serial;

import io.onedev.commons.utils.ExplicitException;

public class BlobEditException extends ExplicitException {

	@Serial
	private static final long serialVersionUID = 1L;

	public BlobEditException(String message) {
		super(message);
	}

}
