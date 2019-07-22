package io.onedev.server.web.page.project.blob.render.renderers.gitlink;

import org.apache.wicket.request.resource.CssResourceReference;

public class GitLinkResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public GitLinkResourceReference() {
		super(GitLinkResourceReference.class, "git-link.css");
	}

}
