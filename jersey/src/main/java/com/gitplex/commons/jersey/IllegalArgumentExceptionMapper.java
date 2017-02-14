package com.gitplex.commons.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
	
	@Override
    public Response toResponse(IllegalArgumentException exception) {
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    	if (exception.getMessage() != null)
    		builder = builder.entity("Illegal argument: " + exception.getMessage()).type("text/plain");
    	else
    		builder = builder.entity("Illegal argument").type("text/plain");
    	
    	return builder.build();
    }
    
}
