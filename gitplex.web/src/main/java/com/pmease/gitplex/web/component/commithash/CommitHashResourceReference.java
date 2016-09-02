package com.pmease.gitplex.web.component.commithash;

import org.apache.wicket.request.resource.CssResourceReference;

public class CommitHashResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitHashResourceReference() {
		super(CommitHashResourceReference.class, "commit-hash.css");
	}

}
