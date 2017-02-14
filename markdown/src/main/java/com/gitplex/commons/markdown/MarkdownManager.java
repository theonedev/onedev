package com.gitplex.commons.markdown;

public interface MarkdownManager {
	
	/**
	 * Parse specified markdown into raw html without sanitization and transform.
	 * 
	 * @param markdown
	 * 			markdown to be parsed
	 * @return
	 * 			raw html as parsed result
	 */
	String parse(String markdown);

	/**
	 * Sanitize and transform specified html.
	 * 
	 * @param html
	 * 			html to be processed
	 * @return
	 * 			processed html
	 */
	String process(String html);

	/**
	 * Parse specified markdown into raw html, and then sanitize this raw html, 
	 * followed by applying transformers against sanitized html.
	 * 
	 * @param markdown
	 * 			markdown to be parsed and processed
	 * @return
	 * 			parsed and processed html
	 */
	String parseAndProcess(String markdown);

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
