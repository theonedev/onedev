package com.pmease.gitplex.web.page.repository.code.blob.renderer;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.service.FileBlob;

@SuppressWarnings("serial")
public class RawBlobResourceReference extends ResourceReference {

	public static final String RAW_BLOB_RESOURCE = "gitplex-raw-blob";
	
	public static PageParameters paramsOf(FileBlob blob) {
		return RepositoryPage.paramsOf(blob.getRepository(), blob.getRevision(), blob.getFilePath());
	}
	
	public RawBlobResourceReference() {
		super(RAW_BLOB_RESOURCE);
	}

	@Override
	public IResource getResource() {
		return new RawBlobResource();
	}

}
