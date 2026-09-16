package io.onedev.server.rest;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
		return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(errorMessage).build();
    }
    
}