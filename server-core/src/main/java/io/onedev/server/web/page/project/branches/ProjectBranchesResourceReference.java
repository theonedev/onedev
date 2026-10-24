package io.onedev.server.web.page.project.branches;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class ProjectBranchesResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectBranchesResourceReference() {
		super(ProjectBranchesResourceReference.class, "project-branches.css");
	}

}
