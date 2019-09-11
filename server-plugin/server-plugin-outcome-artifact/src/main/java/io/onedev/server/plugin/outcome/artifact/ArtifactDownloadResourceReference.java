package io.onedev.server.plugin.outcome.artifact;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ArtifactDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ArtifactDownloadResourceReference() {
		super("build-artifact");
	}

	@Override
	public IResource getResource() {
		return new ArtifactDownloadResource();
	}

}
