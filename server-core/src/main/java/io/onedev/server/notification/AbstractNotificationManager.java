package io.onedev.server.notification;

import javax.annotation.Nullable;

import io.onedev.server.event.Event;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.util.markdown.MarkdownManager;

public abstract class AbstractNotificationManager {

	protected final MarkdownManager markdownManager;
	
	public AbstractNotificationManager(MarkdownManager markdownManager) {
		this.markdownManager = markdownManager;
	}
	
	protected String getHtmlBody(@Nullable Event event, String url) {
		String htmlBody = null;
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware) event).getMarkdown();
			if (markdown != null) {
				Project project = null;
				if (event instanceof ProjectEvent)
					project = ((ProjectEvent) event).getProject();
				String html = markdownManager.process(markdownManager.render(markdown), project, null, true);
				
				htmlBody = String.format(""
						+ "%s"
						+ "<br>"
						+ "<br>"
						+ "<div style='color:#888;font-size:0.9rem;'>Visit <a href='%s'>%s</a> for details</div>", 
						html, url, url);
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
