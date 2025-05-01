package io.onedev.server.persistence.exception;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletResponse;

import io.onedev.server.exception.HttpResponse;
import io.onedev.server.exception.handler.AbstractExceptionHandler;

public class FileNotFoundExceptionHandler extends AbstractExceptionHandler<FileNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(FileNotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
