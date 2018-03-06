package io.onedev.server.rest.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authc.IncorrectCredentialsException;

@Provider
public class IncorrectCredentialsExceptionMapper implements ExceptionMapper<IncorrectCredentialsException> {
	
	@Override
    public Response toResponse(IncorrectCredentialsException exception) {
		return Response.status(Response.Status.UNAUTHORIZED).entity("Incorrect credentials").type("text/plain").build();
    }
    
}
