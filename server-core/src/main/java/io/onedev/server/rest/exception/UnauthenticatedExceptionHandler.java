package io.onedev.server.rest.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.shiro.authz.UnauthenticatedException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class UnauthenticatedExceptionHandler extends AbstractExceptionHandler<UnauthenticatedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Response getResponse(UnauthenticatedException exception) {
		ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
		builder.header("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"OneDev\"");
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Not authenticated").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
