package io.onedev.server.web.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.wicket.core.request.mapper.StalePageException;

import io.onedev.server.exception.AbstractExceptionHandler;

public class StalePageExceptionHandler extends AbstractExceptionHandler<StalePageException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(StalePageException exception) {
		ResponseBuilder builder = Response.status(Response.Status.REQUEST_TIMEOUT);
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
    	else
    		builder = builder.entity("Stale page").type(MediaType.TEXT_PLAIN);
    	
    	return builder.build();
    }
    
}
