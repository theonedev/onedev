package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public class RawBlobResourceReference extends ResourceReference {

	public static final String RAW_BLOB_RESOURCE = "gitop-raw-blob";
	
	public static PageParameters newParams(Long projectId, String revision, String path) {
		PageParameters params = new PageParameters();
		params.set("project", projectId);
		params.set("objectId", revision);
		params.set("path", path);
		
		return params;
	}
	
	public RawBlobResourceReference() {
		super(RAW_BLOB_RESOURCE);
	}

	@Override
	public IResource getResource() {
		return new RawBlobResource();
	}

}
