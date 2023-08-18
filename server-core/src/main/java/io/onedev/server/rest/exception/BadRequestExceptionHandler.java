package io.onedev.server.rest.exception;

import io.onedev.server.exception.AbstractExceptionHandler;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class BadRequestExceptionHandler extends AbstractExceptionHandler<BadRequestException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(BadRequestException exception) {
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    	builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	return builder.build();
    }
    
}
