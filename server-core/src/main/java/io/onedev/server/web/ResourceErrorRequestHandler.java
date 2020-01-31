package io.onedev.server.web;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;

import io.onedev.server.util.ExceptionUtils;

public final class ResourceErrorRequestHandler implements IRequestHandler {
	
	private final Exception exception;
	
	public ResourceErrorRequestHandler(Exception exception) {
		this.exception = exception;
	}

	@Override
	public void respond(IRequestCycle requestCycle) {
		HttpServletResponse response = (HttpServletResponse) requestCycle.getResponse().getContainerResponse();
		ExceptionUtils.handle(response, exception);
	}

	@Override
	public void detach(final IRequestCycle requestCycle) {
	}
	
}
