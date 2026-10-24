package io.onedev.server.web.resource;

import java.io.Serial;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class AttachmentResourceReference extends ResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public AttachmentResourceReference() {
		super("attachment");
	}

	@Override
	public IResource getResource() {
		return new AttachmentResource();
	}

}
