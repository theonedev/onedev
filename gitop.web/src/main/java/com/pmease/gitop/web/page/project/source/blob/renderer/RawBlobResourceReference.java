package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class RawBlobResourceReference extends ResourceReference {

	public static final String RAW_BLOB_RESOURCE = "gitop-raw-blob";
	
	public static PageParameters newParams(FileBlob blob) {
		return newParams(blob.getProject(), blob.getRevision(), blob.getFilePath());
	}
	
	public static PageParameters newParams(Project project, String revision, String path) {
		PageParameters params = PageSpec.forProject(project);
		params.set("objectId", revision);
		PageSpec.addPathToParameters(path, params);
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
