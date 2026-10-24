package io.onedev.server.web.page.project.tags;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class ProjectTagsResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectTagsResourceReference() {
		super(ProjectTagsResourceReference.class, "project-tags.css");
	}

}
