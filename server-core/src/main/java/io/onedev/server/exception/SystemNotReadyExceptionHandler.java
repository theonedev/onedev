package io.onedev.server.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class SystemNotReadyExceptionHandler extends AbstractExceptionHandler<ServerNotReadyException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(ServerNotReadyException exception) {
		ResponseBuilder builder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
    	builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	return builder.build();
    }
    
}
