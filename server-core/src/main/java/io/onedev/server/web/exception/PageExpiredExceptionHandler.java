package io.onedev.server.web.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.wicket.protocol.http.PageExpiredException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class PageExpiredExceptionHandler extends AbstractExceptionHandler<PageExpiredException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(PageExpiredException exception) {
		ResponseBuilder builder = Response.status(Response.Status.REQUEST_TIMEOUT);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Page expired").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
