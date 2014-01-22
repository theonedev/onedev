package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.request.resource.IResource;

public class ImageBlobResourceReference extends RawBlobResourceReference {

	private static final long serialVersionUID = 1L;
	
	public ImageBlobResourceReference() {
	}

	@Override
	public IResource getResource() {
		return new ImageBlobResource();
	}
}
