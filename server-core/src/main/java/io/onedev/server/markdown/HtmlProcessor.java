package io.onedev.server.markdown;

import org.jspecify.annotations.Nullable;

import org.jsoup.nodes.Document;

import io.onedev.commons.loader.ExtensionPoint;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

@ExtensionPoint
public interface HtmlProcessor {
	
	void process(Document document, @Nullable Project project,
				 @Nullable BlobRenderContext blobRenderContext,
				 @Nullable SuggestionSupport suggestionSupport,
				 boolean forExternal);
	
}
