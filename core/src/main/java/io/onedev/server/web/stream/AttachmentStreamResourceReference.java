package io.onedev.server.web.stream;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class AttachmentStreamResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public AttachmentStreamResourceReference() {
		super("attachment");
	}

	@Override
	public IResource getResource() {
		return new AttachmentStreamResource();
	}

}
