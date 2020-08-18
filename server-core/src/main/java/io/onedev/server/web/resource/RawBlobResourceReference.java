package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class RawBlobResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public RawBlobResourceReference() {
		super("raw-blob");
	}

	@Override
	public IResource getResource() {
		return new RawBlobResource();
	}

}
