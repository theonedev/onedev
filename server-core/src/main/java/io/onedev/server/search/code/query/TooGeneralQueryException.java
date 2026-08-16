package io.onedev.server.search.code.query;

import io.onedev.server.exception.NotAcceptableException;

public class TooGeneralQueryException extends NotAcceptableException {

	private static final long serialVersionUID = 1L;
	
	public TooGeneralQueryException(String message) {
		super(message);
	}

}
