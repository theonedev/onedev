package io.onedev.server.notification;

import javax.annotation.Nullable;

import io.onedev.server.event.Event;
import io.onedev.server.event.MarkdownAware;

public abstract class AbstractNotificationManager {

	protected String getHtmlBody(@Nullable Event event, String url) {
		String htmlBody = null;
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware) event).getMarkdown();
			if (markdown != null) {
				htmlBody = String.format(""
						+ "<pre>%s</pre>"
						+ "<p>"
						+ "Visit <a href='%s'>%s</a> for details", 
						markdown, url, url);
			}
		}
		if (htmlBody == null)
			htmlBody = String.format("Visit <a href='%s'>%s</a> for details", url, url);
		return htmlBody;
	}
	
	protected String getTextBody(@Nullable Event event, String url) {
		String textBody = null;
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware) event).getMarkdown();
			if (markdown != null) {
				textBody = String.format(""
						+ "%s"
						+ "\n\n"
						+ "Visit %s for details", 
						markdown, url);
			}
		}
		if (textBody == null)
			textBody = String.format("Visit %s for details", url);
		return textBody;
	}
	
}
