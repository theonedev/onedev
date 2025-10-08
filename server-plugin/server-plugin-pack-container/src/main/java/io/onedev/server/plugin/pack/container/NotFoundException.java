package io.onedev.server.plugin.pack.container;

import org.jspecify.annotations.Nullable;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class NotFoundException extends ClientException {

	public NotFoundException(ErrorCode errorCode, @Nullable String errorMessage) {
		super(SC_NOT_FOUND, errorCode, errorMessage);
	}

	public NotFoundException(ErrorCode errorCode) {
		this(errorCode, null);
	}
	
}
