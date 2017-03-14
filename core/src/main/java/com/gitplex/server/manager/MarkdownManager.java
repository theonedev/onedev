package com.gitplex.server.manager;

import com.vladsch.flexmark.util.options.MutableDataHolder;

public interface MarkdownManager {
	
	/**
	 * Render specified markdown into html
	 * 
	 * @param markdown
	 * 			markdown to be rendered
	 * @param postProcess
	 * 			whether or not to post-process rendered html
	 * @return
	 * 			rendered html
	 */
	String render(String markdown, boolean postProcess);

	/**
	 * Sanitize and transform specified html.
	 * 
	 * @param html
	 * 			html to be post-processed
	 * @return
	 * 			post-processed html
	 */
	String postProcess(String html);
	
	/**
	 * Escape html characters in specified markdown so that the markdown plain text 
	 * can be embedded in html content such as html email.
	 * 
	 * @param markdown
	 * 			markdown to be escaped
	 * @return
	 * 			escaped markdown plain text
	 */
	String escape(String markdown);
	
	MutableDataHolder getOptions();
	
}
