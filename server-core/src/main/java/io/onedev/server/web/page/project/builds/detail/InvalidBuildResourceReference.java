package io.onedev.server.web.page.project.builds.detail;

import org.apache.wicket.request.resource.CssResourceReference;

public class InvalidBuildResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public InvalidBuildResourceReference() {
		super(InvalidBuildResourceReference.class, "invalid-build.css");
	}

}
