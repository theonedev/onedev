package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;

import io.onedev.server.exception.HttpResponse;

public class AuthenticationExceptionHandler extends AbstractExceptionHandler<AuthenticationException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(AuthenticationException exception) {
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, 
				"User unknown or credential incorrect");
    }
    
}
