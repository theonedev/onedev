package io.onedev.server.web.component.codecomment;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class CodeCommentCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public CodeCommentCssResourceReference() {
		super(CodeCommentCssResourceReference.class, "code-comment.css");
	}

}
