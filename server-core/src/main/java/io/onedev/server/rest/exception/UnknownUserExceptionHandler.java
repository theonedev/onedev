package io.onedev.server.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authc.UnknownAccountException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class UnknownUserExceptionHandler extends AbstractExceptionHandler<UnknownAccountException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(UnknownAccountException exception) {
		return Response
				.status(Response.Status.UNAUTHORIZED)
				.entity("Invalid credentials")
				.type(MediaType.TEXT_PLAIN)
				.build();
    }
    
}
