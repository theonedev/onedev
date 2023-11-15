package io.onedev.server.exception.handler;

import io.onedev.server.exception.ChallengeAwareUnauthenticatedException;
import io.onedev.server.exception.HttpResponse;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ChallengeAwareUnauthenticatedExceptionHandler 
		extends AbstractExceptionHandler<ChallengeAwareUnauthenticatedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public HttpResponse getResponse(ChallengeAwareUnauthenticatedException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not authenticated";
		
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.putSingle("WWW-Authenticate", exception.getChallenge());
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, errorMessage, headers);
    }
    
}
