package io.onedev.server.exception.handler;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.exception.HttpResponse;

public class ConversionExceptionHandler extends AbstractExceptionHandler<ConversionException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ConversionException exception) {
		if (exception.getMessage() != null)
			return new HttpResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, exception.getMessage());
		else 
			return null;
    }
    
}
