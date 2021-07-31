package io.onedev.server.markdown;

import javax.annotation.Nullable;

import org.jsoup.nodes.Document;

import com.vladsch.flexmark.ast.Node;

import io.onedev.server.model.Project;

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
	
	Document process(Document document, @Nullable Project project, @Nullable Object context, boolean forExternal);

	String process(String html, @Nullable Project project, @Nullable Object context, boolean forExternal);
	
}
