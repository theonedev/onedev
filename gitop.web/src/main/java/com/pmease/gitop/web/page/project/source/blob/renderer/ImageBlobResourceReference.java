package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.SharedResourceReference;

public class ImageBlobResourceReference extends SharedResourceReference {

	private static final long serialVersionUID = 1L;
	
	public static String IMAGE_BLOB_RESOURCE = "gitop-image-blob-resource";
	
	public ImageBlobResourceReference() {
		super(IMAGE_BLOB_RESOURCE);
	}

	@Override
	public IResource getResource() {
		return new ImageBlobResource();
	}
}
