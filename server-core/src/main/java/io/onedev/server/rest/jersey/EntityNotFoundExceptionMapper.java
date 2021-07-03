package io.onedev.server.rest.jersey;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {
	
	@Override
    public Response toResponse(EntityNotFoundException exception) {
		ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type("text/plain");
    	else
    		builder = builder.entity("Not found").type("text/plain");
    	
    	return builder.build();
    }
    
}
