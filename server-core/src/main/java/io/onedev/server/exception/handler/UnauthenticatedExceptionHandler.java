package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.shiro.authz.UnauthenticatedException;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.HttpResponseBody;

public class UnauthenticatedExceptionHandler extends AbstractExceptionHandler<UnauthenticatedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public HttpResponse getResponse(UnauthenticatedException exception) {
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.putSingle("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"OneDev\"");
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not authenticated";
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, 
				new HttpResponseBody(false, errorMessage), headers);
    }
    
}
