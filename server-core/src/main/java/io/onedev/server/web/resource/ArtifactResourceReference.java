package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ArtifactResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ArtifactResourceReference() {
		super("artifact");
	}

	@Override
	public IResource getResource() {
		return new ArtifactResource();
	}

}
