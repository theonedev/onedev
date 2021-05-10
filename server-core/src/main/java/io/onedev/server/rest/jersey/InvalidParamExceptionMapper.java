package io.onedev.server.rest.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidParamExceptionMapper implements ExceptionMapper<InvalidParamException> {
	
	@Override
    public Response toResponse(InvalidParamException exception) {
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
    	builder = builder.entity(exception.getMessage()).type("text/plain");
    	return builder.build();
    }
    
}
