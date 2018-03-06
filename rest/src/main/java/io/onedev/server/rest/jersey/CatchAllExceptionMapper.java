package io.onedev.server.rest.jersey;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class CatchAllExceptionMapper implements ExceptionMapper<Throwable> {
	
    private static final Logger logger = LoggerFactory.getLogger(CatchAllExceptionMapper.class);

    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            return ((WebApplicationException)t).getResponse();
        } else {
            logger.error("Uncaught exception thrown by RESTful service", t);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            		.entity("Http response code 500: internal server error: check server log for details")
            		.type(MediaType.TEXT_PLAIN)
            		.build();
        }
    }
}