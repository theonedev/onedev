package io.onedev.server.web.stream;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ServerLogStreamResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ServerLogStreamResourceReference() {
		super("server-log");
	}

	@Override
	public IResource getResource() {
		return new ServerLogStreamResource();
	}

}
