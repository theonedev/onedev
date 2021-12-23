package io.onedev.server.persistence.exception;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.onedev.server.exception.AbstractExceptionHandler;

public class EntityNotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(EntityNotFoundException exception) {
		ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Database entity not found").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
