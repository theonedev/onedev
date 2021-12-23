package io.onedev.server.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.onedev.commons.utils.ExplicitException;

public class ExplicitExceptionHandler extends AbstractExceptionHandler<ExplicitException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(ExplicitException exception) {
		ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
    	builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	return builder.build();
    }
    
}
