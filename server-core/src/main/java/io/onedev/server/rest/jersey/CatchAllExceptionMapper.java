package io.onedev.server.rest.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.ExceptionUtils;

@Provider
public class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {
	
    private static final Logger logger = LoggerFactory.getLogger(CatchAllExceptionMapper.class);

    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            return ((WebApplicationException)t).getResponse();
        } else {
            String errorMessage = ExceptionUtils.getExpectedError(t);
            if (errorMessage != null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                		.entity(errorMessage).type(MediaType.TEXT_PLAIN).build();
            } else {
            	logger.error("Error handling restful service", t);
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	            		.entity("Http response code 500: internal server error: check server log for details")
	            		.type(MediaType.TEXT_PLAIN)
	            		.build();
            }
        }
    }
}