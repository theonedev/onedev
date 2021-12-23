package io.onedev.server.rest.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class UnauthorizedExceptionHandler extends AbstractExceptionHandler<UnauthorizedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Response getResponse(UnauthorizedException exception) {
		ResponseBuilder builder;
		if (!SecurityUtils.getSubject().isAuthenticated()) {
			builder = Response.status(Response.Status.UNAUTHORIZED);
			builder.header("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"OneDev\"");
		} else {
			builder = Response.status(Response.Status.FORBIDDEN);
		}
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Permission denied").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
