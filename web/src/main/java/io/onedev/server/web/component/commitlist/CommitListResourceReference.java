package io.onedev.server.web.component.commitlist;

import org.apache.wicket.request.resource.CssResourceReference;

public class CommitListResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitListResourceReference() {
		super(CommitListResourceReference.class, "commit-list.css");
	}

}
