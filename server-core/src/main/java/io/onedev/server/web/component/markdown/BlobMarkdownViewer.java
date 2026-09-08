package io.onedev.server.web.component.markdown;

import org.apache.wicket.model.IModel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.util.WikiLinkResolver;

public class BlobMarkdownViewer extends MarkdownViewer {

	public BlobMarkdownViewer(String id, IModel<String> model,
			@Nullable ContentVersionSupport contentVersionSupport) {
		super(id, model, contentVersionSupport);
	}

	@Override
	protected String renderMarkdown(String markdown) {
		return WikiLinkResolver.resolveWikiLinks(super.renderMarkdown(markdown), getRenderContext());
	}

}
