package io.onedev.server.web.download;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class BuildLogDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public BuildLogDownloadResourceReference() {
		super("build-log");
	}

	@Override
	public IResource getResource() {
		return new BuildLogDownloadResource();
	}

}
