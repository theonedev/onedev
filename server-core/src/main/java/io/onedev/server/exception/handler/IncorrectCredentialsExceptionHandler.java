package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;
import org.apache.shiro.authc.IncorrectCredentialsException;

import javax.servlet.http.HttpServletResponse;

public class IncorrectCredentialsExceptionHandler extends AbstractExceptionHandler<IncorrectCredentialsException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(IncorrectCredentialsException exception) {
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED, 
				"User unknown or credential incorrect");
    }
    
}
