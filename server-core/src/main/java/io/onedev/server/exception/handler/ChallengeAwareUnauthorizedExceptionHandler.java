package io.onedev.server.exception.handler;

import io.onedev.server.exception.ChallengeAwareUnauthorizedException;
import io.onedev.server.exception.HttpResponse;
import io.onedev.server.security.SecurityUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class ChallengeAwareUnauthorizedExceptionHandler 
		extends AbstractExceptionHandler<ChallengeAwareUnauthorizedException> {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public HttpResponse getResponse(ChallengeAwareUnauthorizedException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not authorized";
		
		if (SecurityUtils.getUserId().equals(0L)) {
			MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			headers.putSingle("WWW-Authenticate", exception.getChallenge());
			return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, errorMessage, headers);
		} else {
			return new HttpResponse(HttpServletResponse.SC_FORBIDDEN, errorMessage);
		}
    }
    
}
