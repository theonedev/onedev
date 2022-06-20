package io.onedev.server.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import com.vladsch.flexmark.util.ast.Node;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

public interface MarkdownManager {
	
	/**
	 * Render specified markdown into html
	 * 
	 * @param markdown
	 * 			markdown to be rendered
	 * 			
	 * @return
	 * 			rendered html
	 */
	String render(String markdown);
	
	Node parse(String markdown);
	
	Document process(Document document, @Nullable Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, 
			boolean forExternal);

	String process(String html, @Nullable Project project, 
			@Nullable BlobRenderContext blobRenderContext, 
			@Nullable SuggestionSupport suggestionSupport, 
			boolean forExternal);
	
}
