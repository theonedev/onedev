package io.onedev.server.ee.pack.container;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class BadRequestException extends ClientException {

	public BadRequestException(String errorMessage) {
		super(SC_BAD_REQUEST, ErrorCode.DENIED, errorMessage);
	}
}
