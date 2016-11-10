package com.gitplex.commons.jersey;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authc.UnknownAccountException;

@Provider
public class UnknownAccountExceptionMapper implements ExceptionMapper<UnknownAccountException> {
	
	@Override
    public Response toResponse(UnknownAccountException exception) {
		return Response.status(Response.Status.UNAUTHORIZED).entity("Unknown account").type("text/plain").build();
    }
    
}
