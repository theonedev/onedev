package io.onedev.server.util.commenttext;

import io.onedev.server.OneDev;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.Project;

public class MarkdownText extends CommentText {

	private final Project project;
	
	private transient String rendered;
	
	private transient String processed;
	
	private transient String plainContent;
	
	public MarkdownText(Project project, String content) {
		super(content);
		this.project = project;
	}
	
	private MarkdownService getMarkdownService() {
		return OneDev.getInstance(MarkdownService.class);
	}
	
	public String getRendered() {
		if (rendered == null) 
			rendered = getMarkdownService().render(getContent());
		return rendered;
	}
	
	public String getProcessed() {
		if (processed == null)
			processed = getMarkdownService().process(getRendered(), project, null, null, true);
		return processed;
	}

	@Override
	public String getHtmlContent() {
		return getProcessed();
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
