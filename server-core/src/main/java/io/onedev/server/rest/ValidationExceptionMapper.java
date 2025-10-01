package io.onedev.server.rest;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Must have this although we have a general JerseyExceptionMapper (and in turn takes care of ValidationException 
 * via exception handler), as Jersey has an internal ValidationExceptionMapper whose parameter for ExceptionMapper 
 * has smaller distance to ValidationException than JerseyExceptionMapper (Jersey uses this to find the most 
 * relevant exception mapper for a given exception). 
 */
@Provider
@Priority(1)
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	
    @Override
    public Response toResponse(ValidationException t) {
		var errorMessage = t.getMessage();
		if (errorMessage == null)
			errorMessage = "Validation error";
		return Response.status(HttpServletResponse.SC_NOT_ACCEPTABLE).entity(errorMessage).build();
    }
    
}