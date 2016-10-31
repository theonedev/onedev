package com.gitplex.web.component.commitmessage;

import org.apache.wicket.request.resource.CssResourceReference;

public class CommitMessageResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitMessageResourceReference() {
		super(CommitMessageResourceReference.class, "commit-message.css");
	}

}
