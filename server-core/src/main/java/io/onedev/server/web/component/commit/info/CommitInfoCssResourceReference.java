package io.onedev.server.web.component.commit.info;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CommitInfoCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CommitInfoCssResourceReference() {
		super(CommitInfoCssResourceReference.class, "commit-info.css");
	}

}
