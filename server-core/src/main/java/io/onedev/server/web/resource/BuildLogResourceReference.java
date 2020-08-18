package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class BuildLogResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildLogResourceReference() {
		super("build-log");
	}

	@Override
	public IResource getResource() {
		return new BuildLogResource();
	}

}
