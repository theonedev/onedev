package io.onedev.server.rest.jersey;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.ExceptionUtils;

@Provider
public class JerseyExceptionMapper implements ExceptionMapper<Throwable> {
	
    private static final Logger logger = LoggerFactory.getLogger(JerseyExceptionMapper.class);

    @Override
    public Response toResponse(Throwable t) {
    	Response response = ExceptionUtils.buildResponse(t);
    	if (response == null) {
        	logger.error("Error handling restful service", t);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            		.entity("Internal server error: check server log for details")
            		.type(MediaType.TEXT_PLAIN)
            		.build();
        }
    	return response;
    }
    
}