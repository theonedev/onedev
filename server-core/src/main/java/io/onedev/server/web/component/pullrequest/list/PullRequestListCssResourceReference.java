package io.onedev.server.web.component.pullrequest.list;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class PullRequestListCssResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PullRequestListCssResourceReference() {
		super(PullRequestListCssResourceReference.class, "pull-request-list.css");
	}

}
