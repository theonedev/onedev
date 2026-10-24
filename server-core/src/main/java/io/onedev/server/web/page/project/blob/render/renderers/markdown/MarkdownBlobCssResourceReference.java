package io.onedev.server.web.page.project.blob.render.renderers.markdown;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class MarkdownBlobCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MarkdownBlobCssResourceReference() {
		super(MarkdownBlobCssResourceReference.class, "markdown-blob.css");
	}

}
