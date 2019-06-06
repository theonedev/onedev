package io.onedev.server.web.page.project.stats;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

class TopContributorsResourceReference extends ResourceReference {

	public static final String NAME = "topContributors";
	
	public TopContributorsResourceReference() {
		super(NAME);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public IResource getResource() {
		return new TopContributorsResource();
	}

}
