package io.onedev.server.exception;

import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class ValidationExceptionHandler extends AbstractExceptionHandler<ValidationException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public Response getResponse(ValidationException exception) {
		ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
		if (exception.getMessage() != null)
			builder = builder.entity(exception.getMessage()).type(MediaType.TEXT_PLAIN);
		else
			builder = builder.entity("Validation failed").type(MediaType.TEXT_PLAIN);
			
    	return builder.build();
    }
    
}
