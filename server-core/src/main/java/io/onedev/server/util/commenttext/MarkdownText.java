package io.onedev.server.util.commenttext;

import io.onedev.server.OneDev;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.MarkdownManager;
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
	
	private MarkdownManager getMarkdownManager() {
		return OneDev.getInstance(MarkdownManager.class);
	}
	
	public String getRendered() {
		if (rendered == null) 
			rendered = getMarkdownManager().render(getContent());
		return rendered;
	}
	
	public String getProcessed() {
		if (processed == null)
			processed = getMarkdownManager().process(getRendered(), project, null, null, true);
		return processed;
	}

	@Override
	public String getHtmlContent() {
		return getProcessed();
	}

	@Override
	public String getPlainContent() {
		if (plainContent == null) {
			MailManager mailManager = OneDev.getInstance(MailManager.class);
			if (mailManager.isMailContent(getContent()))  
				plainContent = mailManager.toPlainText(getContent());
			else
				plainContent = getContent();
		}
		return plainContent;
	}
	
}
