package io.onedev.server.rest.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import io.onedev.server.git.exception.ObjectNotFoundException;

@Provider
public class GitObjectNotFoundExceptionMapper implements ExceptionMapper<ObjectNotFoundException> {
	
	@Override
    public Response toResponse(ObjectNotFoundException exception) {
		ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type("text/plain");
    	else
    		builder = builder.entity("Not found").type("text/plain");
    	
    	return builder.build();
    }
    
}
