package io.onedev.server.web.asset.revisioncompare;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class RevisionCompareCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public RevisionCompareCssResourceReference() {
		super(RevisionCompareCssResourceReference.class, "revision-compare.css");
	}

}
