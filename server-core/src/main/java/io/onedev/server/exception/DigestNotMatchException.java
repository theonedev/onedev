package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class DigestNotMatchException extends ExplicitException {
	
	public DigestNotMatchException() {
		super("Digest not matching");
	}
	
}
