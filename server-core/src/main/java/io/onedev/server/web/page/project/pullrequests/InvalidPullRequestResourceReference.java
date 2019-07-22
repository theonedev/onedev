package io.onedev.server.web.page.project.pullrequests;

import org.apache.wicket.request.resource.CssResourceReference;

public class InvalidPullRequestResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public InvalidPullRequestResourceReference() {
		super(InvalidPullRequestResourceReference.class, "invalid-pullrequest.css");
	}

}
