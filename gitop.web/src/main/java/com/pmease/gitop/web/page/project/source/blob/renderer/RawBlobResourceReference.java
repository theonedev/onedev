package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.service.FileBlob;

@SuppressWarnings("serial")
public class RawBlobResourceReference extends ResourceReference {

	public static final String RAW_BLOB_RESOURCE = "gitop-raw-blob";
	
	public static PageParameters newParams(FileBlob blob) {
		PageParameters params = new PageParameters();
		Long projectId = blob.getProjectId();
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		
		params.set(PageSpec.USER, project.getOwner().getName());
		params.set(PageSpec.PROJECT, project.getName());
		params.set("objectId", blob.getRevision());
		
		PageSpec.addPathToParameters(blob.getFilePath(), params);
		
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
