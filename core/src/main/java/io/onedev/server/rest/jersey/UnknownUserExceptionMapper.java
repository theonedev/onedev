package io.onedev.server.rest.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authc.UnknownAccountException;

@Provider
public class UnknownUserExceptionMapper implements ExceptionMapper<UnknownAccountException> {
	
	@Override
    public Response toResponse(UnknownAccountException exception) {
		return Response.status(Response.Status.UNAUTHORIZED).entity("Unknown user").type("text/plain").build();
    }
    
}
