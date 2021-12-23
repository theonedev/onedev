package io.onedev.server.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class SystemNotReadyExceptionHandler extends AbstractExceptionHandler<SystemNotReadyException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(SystemNotReadyException exception) {
		ResponseBuilder builder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
    	builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	return builder.build();
    }
    
}
