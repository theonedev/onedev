package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.ServerNotReadyException;

import javax.servlet.http.HttpServletResponse;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

public class ServerNotReadyExceptionHandler extends AbstractExceptionHandler<ServerNotReadyException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ServerNotReadyException exception) {
		return new HttpResponse(HttpServletResponse.SC_SERVICE_UNAVAILABLE, exception.getMessage());
    }
    
}
