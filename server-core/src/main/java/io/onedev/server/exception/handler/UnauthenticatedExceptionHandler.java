package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;
import org.apache.shiro.authz.UnauthenticatedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

public class UnauthenticatedExceptionHandler extends AbstractExceptionHandler<UnauthenticatedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public HttpResponse getResponse(UnauthenticatedException exception) {
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.putSingle("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"OneDev\"");
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not authenticated";
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, errorMessage, headers);
    }
    
}
