package io.onedev.server.util.commenttext;

import java.util.Collection;

import io.onedev.server.OneDev;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.Project;

public class MarkdownText extends CommentText {

	private final Project project;
	
	private transient String rawHtml;
	
	private transient String htmlContent;
	
	private transient String plainContent;
	
	public MarkdownText(Project project, String content) {
		super(content);
		this.project = project;
	}
	
	private MarkdownService getMarkdownService() {
		return OneDev.getInstance(MarkdownService.class);
	}
	
	private String getRawHtml() {
		if (rawHtml == null)
			rawHtml = getMarkdownService().renderRawHtml(getContent());
		return rawHtml;
	}

	public Collection<String> getMentions() {
		return new MentionParser().parseMentions(getRawHtml());
	}

	@Override
	public String getHtmlContent() {
		if (htmlContent == null)
			htmlContent = getMarkdownService().convertToExternal(getRawHtml(), project);
		return htmlContent;
	}

	@Override
	public String getPlainContent() {
		if (plainContent == null) {
			MailService mailService = OneDev.getInstance(MailService.class);
			if (mailService.isMailContent(getContent()))  
				plainContent = mailService.toPlainText(getContent());
			else
				plainContent = getContent();
		}
		return plainContent;
	}
	
}
