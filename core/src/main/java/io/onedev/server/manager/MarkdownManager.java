package io.onedev.server.manager;

import javax.annotation.Nullable;

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
	
	String process(String rendered, @Nullable Object context);

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
	
}
