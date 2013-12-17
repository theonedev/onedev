package com.pmease.gitop.web.util;

import static org.pegdown.Extensions.ALL;
import static org.pegdown.Extensions.SMARTYPANTS;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;

/**
 * Utility methods for transforming raw markdown text to html.
 *
 * @author James Moger
 *
 */

public class MarkdownUtils {

	/**
	 * Returns the html version of the plain source text.
	 *
	 * @param text
	 * @return html version of plain text
	 * @throws java.text.ParseException
	 */
	public static String transformPlainText(String text) {
		// url auto-linking
		text = text.replaceAll("((http|https)://[0-9A-Za-z-_=\\?\\.\\$#&/]*)", "<a href=\"$1\">$1</a>");
		String html = "<pre>" + text + "</pre>";
		return html;
	}


	/**
	 * Returns the html version of the markdown source text.
	 *
	 * @param markdown
	 * @return html version of markdown text
	 * @throws java.text.ParseException
	 */
	public static String transformMarkdown(String markdown) {
		return transformMarkdown(markdown, null);
	}

	/**
	 * Returns the html version of the markdown source text.
	 *
	 * @param markdown
	 * @return html version of markdown text
	 * @throws java.text.ParseException
	 */
	public static String transformMarkdown(String markdown, LinkRenderer linkRenderer) {
		PegDownProcessor pd = new PegDownProcessor(ALL & ~SMARTYPANTS);
		String html = pd.markdownToHtml(markdown, linkRenderer == null ? new LinkRenderer() : linkRenderer);
		return html;
	}

	/**
	 * Returns the html version of the markdown source reader. The reader is
	 * closed regardless of success or failure.
	 *
	 * @param markdownReader
	 * @return html version of the markdown text
	 * @throws java.text.ParseException
	 */
	public static String transformMarkdown(Reader markdownReader) throws IOException {
		// Read raw markdown content and transform it to html
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(markdownReader, writer);
			String markdown = writer.toString();
			return transformMarkdown(markdown);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// IGNORE
			}
		}
	}
}
