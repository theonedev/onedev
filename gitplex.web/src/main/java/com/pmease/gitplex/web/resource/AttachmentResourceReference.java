package com.pmease.gitplex.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class AttachmentResourceReference extends ResourceReference {

	public static final String NAME = "attachment";
	
	public AttachmentResourceReference() {
		super(NAME);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public IResource getResource() {
		return new AttachmentResource();
	}

}
