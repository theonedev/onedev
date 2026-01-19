package io.onedev.server.web.page.project.blob.render.renderers.gitlink;

import org.apache.wicket.request.resource.CssResourceReference;

public class GitLinkCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public GitLinkCssResourceReference() {
		super(GitLinkCssResourceReference.class, "git-link.css");
	}

}
