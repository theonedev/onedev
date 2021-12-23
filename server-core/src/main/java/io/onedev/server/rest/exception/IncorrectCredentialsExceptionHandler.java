package io.onedev.server.rest.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authc.IncorrectCredentialsException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class IncorrectCredentialsExceptionHandler extends AbstractExceptionHandler<IncorrectCredentialsException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(IncorrectCredentialsException exception) {
		return Response
				.status(Response.Status.UNAUTHORIZED)
				.entity("Incorrect credentials")
				.type(MediaType.TEXT_PLAIN)
				.build();
    }
    
}
