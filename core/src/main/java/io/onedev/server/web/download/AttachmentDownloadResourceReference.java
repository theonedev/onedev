package io.onedev.server.web.download;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class AttachmentDownloadResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public AttachmentDownloadResourceReference() {
		super("attachment");
	}

	@Override
	public IResource getResource() {
		return new AttachmentDownloadResource();
	}

}
