package io.onedev.server.web.stream;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class BuildLogStreamResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildLogStreamResourceReference() {
		super("build-log");
	}

	@Override
	public IResource getResource() {
		return new BuildLogStreamResource();
	}

}
