package io.onedev.server.web.download;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ServerLogDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ServerLogDownloadResourceReference() {
		super("server-log");
	}

	@Override
	public IResource getResource() {
		return new ServerLogDownloadResource();
	}

}
