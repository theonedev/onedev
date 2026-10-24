package io.onedev.server.web.component.commit.status;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CommitStatusCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CommitStatusCssResourceReference() {
		super(CommitStatusCssResourceReference.class, "commit-status.css");
	}

}
