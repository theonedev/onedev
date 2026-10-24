package io.onedev.server.exception.handler;

import java.io.Serial;
import java.nio.channels.ClosedChannelException;

import io.onedev.server.exception.HttpResponse;

public class ClosedChannelExceptionHandler extends AbstractExceptionHandler<ClosedChannelException> {
	
	@Serial
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ClosedChannelException exception) {
		var message = exception.getMessage();
		if (message == null)
			message = "Channel closed";
		return new HttpResponse(499, message);
    }
    
}
