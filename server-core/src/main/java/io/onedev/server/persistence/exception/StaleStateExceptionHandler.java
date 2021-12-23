package io.onedev.server.persistence.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.hibernate.StaleStateException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class StaleStateExceptionHandler extends AbstractExceptionHandler<StaleStateException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(StaleStateException exception) {
		ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Persistence state staled").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
