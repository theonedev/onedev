package io.onedev.server.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.onedev.server.exception.AbstractExceptionHandler;

public class InvalidParamExceptionHandler extends AbstractExceptionHandler<InvalidParamException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(InvalidParamException exception) {
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    	builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	return builder.build();
    }
    
}
