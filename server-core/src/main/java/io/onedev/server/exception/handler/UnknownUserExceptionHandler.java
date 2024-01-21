package io.onedev.server.exception.handler;

import io.onedev.server.exception.HttpResponse;
import org.apache.shiro.authc.UnknownAccountException;

import javax.servlet.http.HttpServletResponse;

public class UnknownUserExceptionHandler extends AbstractExceptionHandler<UnknownAccountException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(UnknownAccountException exception) {
		return new HttpResponse(HttpServletResponse.SC_UNAUTHORIZED,  
				"User unknown or credential incorrect");
    }
    
}
