package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class DataTooLargeException extends ExplicitException {
	public DataTooLargeException(long maxSize) {
		super("Data exceeds maximum size: " + maxSize);
	}
}
