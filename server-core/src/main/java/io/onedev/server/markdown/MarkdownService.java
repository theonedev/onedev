package io.onedev.server.markdown;

import org.jspecify.annotations.Nullable;

import com.vladsch.flexmark.util.ast.Document;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.markdown.SuggestionSupport;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;

public interface MarkdownService {

	/** Renders Markdown for display, sanitizing the HTML and applying OneDev's HTML processors. */
	String renderHtml(String markdown, @Nullable Project project, @Nullable BlobRenderContext blobRenderContext,
			@Nullable SuggestionSupport suggestionSupport);

	/** Parses OneDev Markdown into a syntax tree with source offsets. */
	Document parse(String markdown);

	/** Renders unsanitized HTML for extracting headings, mentions, and references. */
	String renderRawHtml(String markdown);

	/** Sanitizes the output of {@link #renderRawHtml} and rewrites its content, links, and images to be displayed outside of OneDev such as email body. */
	String convertToExternal(String rawHtml, @Nullable Project project);

}
