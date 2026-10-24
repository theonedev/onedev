package io.onedev.server.web.page.project.codecomments;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class ProjectCodeCommentsCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProjectCodeCommentsCssResourceReference() {
		super(ProjectCodeCommentsCssResourceReference.class, "project-code-comments.css");
	}

}
