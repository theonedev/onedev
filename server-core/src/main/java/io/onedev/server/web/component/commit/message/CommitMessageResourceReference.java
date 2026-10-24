package io.onedev.server.web.component.commit.message;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class CommitMessageResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CommitMessageResourceReference() {
		super(CommitMessageResourceReference.class, "commit-message.css");
	}

}
