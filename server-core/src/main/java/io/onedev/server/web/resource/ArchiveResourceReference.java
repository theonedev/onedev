package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ArchiveResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ArchiveResourceReference() {
		super("archive");
	}

	@Override
	public IResource getResource() {
		return new ArchiveResource();
	}

}
