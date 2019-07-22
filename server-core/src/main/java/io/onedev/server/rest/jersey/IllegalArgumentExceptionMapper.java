package io.onedev.server.rest.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
	
	private static final Logger logger = LoggerFactory.getLogger(IllegalArgumentException.class);
	
	@Override
    public Response toResponse(IllegalArgumentException exception) {
		logger.error("Error serving rest request", exception);
		
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    	if (exception.getMessage() != null)
    		builder = builder.entity("Illegal argument: " + exception.getMessage()).type("text/plain");
    	else
    		builder = builder.entity("Illegal argument").type("text/plain");
    	
    	return builder.build();
    }
    
}
