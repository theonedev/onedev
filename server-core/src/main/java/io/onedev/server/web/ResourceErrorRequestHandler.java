package io.onedev.server.web;

import io.onedev.server.exception.ExceptionUtils;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public final class ResourceErrorRequestHandler implements IRequestHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceErrorRequestHandler.class);
	
	private final Exception exception;
	
	public ResourceErrorRequestHandler(Exception exception) {
		this.exception = exception;
	}

	@Override
	public void respond(IRequestCycle requestCycle) {
		HttpServletResponse response = (HttpServletResponse) requestCycle.getResponse().getContainerResponse();
		ExceptionUtils.handle(response, exception);
		if (response.getStatus() >= SC_INTERNAL_SERVER_ERROR)
			logger.error("Error processing resource request", exception);
	}

	@Override
	public void detach(final IRequestCycle requestCycle) {
	}
	
}
