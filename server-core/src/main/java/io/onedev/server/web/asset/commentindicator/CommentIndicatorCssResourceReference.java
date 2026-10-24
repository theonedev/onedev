package io.onedev.server.web.asset.commentindicator;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CommentIndicatorCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CommentIndicatorCssResourceReference() {
		super(CommentIndicatorCssResourceReference.class, "comment-indicator.css");
	}

}
