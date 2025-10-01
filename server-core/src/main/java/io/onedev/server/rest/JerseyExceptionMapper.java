package io.onedev.server.rest;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.exception.ExceptionUtils;

@Provider
public class JerseyExceptionMapper implements ExceptionMapper<Throwable> {
	
	private static final Logger logger = LoggerFactory.getLogger(JerseyExceptionMapper.class);
	
    @Override
    public Response toResponse(Throwable t) {
    	var httpResponse = ExceptionUtils.buildResponse(t);
		Response jerseyResponse;
    	if (httpResponse != null) {
			var jerseyResponseBuilder = Response.status(httpResponse.getStatus());
			if (httpResponse.getBody() != null) {
				jerseyResponseBuilder
						.type(httpResponse.getBody().getContentType())
						.entity(httpResponse.getBody().getText());
			}
			return jerseyResponseBuilder.build();
		} else {
			int statusCode = SC_INTERNAL_SERVER_ERROR;
			jerseyResponse = Response.status(statusCode)
					.type(MediaType.TEXT_PLAIN)
					.entity(HttpStatus.getMessage(statusCode))
					.build();
        }
		if (jerseyResponse.getStatus() >= SC_INTERNAL_SERVER_ERROR) 
			logger.error("Error processing api request", t);
		return jerseyResponse;
    }
    
}