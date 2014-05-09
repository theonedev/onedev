package com.pmease.commons.jersey;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authz.UnauthenticatedException;

import com.google.inject.Key;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppName;

@Provider
public class UnauthenticatedExceptionMapper implements ExceptionMapper<UnauthenticatedException> {
	
    private final String appName = AppLoader.injector.getInstance(Key.<String>get(String.class, AppName.class));
	
	@Override
    public Response toResponse(UnauthenticatedException exception) {
		ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
		builder.header("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"" + appName + "\"");
    	if (exception.getMessage() != null)
    		builder = builder.entity(exception.getMessage()).type("text/plain");
    	
    	return builder.build();
    }
    
}
