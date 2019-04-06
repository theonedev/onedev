package io.onedev.server.web.stream;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ArchiveStreamResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public ArchiveStreamResourceReference() {
		super("archive");
	}

	@Override
	public IResource getResource() {
		return new ArchiveStreamResource();
	}

}
