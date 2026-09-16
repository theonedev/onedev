package io.onedev.server.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.shiro.authc.AuthenticationException;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.HttpResponseBody;
import io.onedev.server.security.SecurityUtils;

public class AuthenticationExceptionHandler extends AbstractExceptionHandler<AuthenticationException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(AuthenticationException exception) {
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("WWW-Authenticate", HttpServletRequest.BASIC_AUTH + " realm=\"OneDev\"");
		headers.add("WWW-Authenticate", "Bearer realm=\"OneDev\"");
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, 
				new HttpResponseBody(false, SecurityUtils.AUTHENTICATION_FAILED_MESSAGE), headers);
    }
    
}
