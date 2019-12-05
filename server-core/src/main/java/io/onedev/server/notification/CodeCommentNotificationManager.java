package io.onedev.server.notification;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.markdown.MentionParser;

@Singleton
public class CodeCommentNotificationManager {
	
	private final MailManager mailManager;
	
	private final MarkdownManager markdownManager;
	
	private final UrlManager urlManager;
	
	@Inject
	public CodeCommentNotificationManager(MailManager mailManager, MarkdownManager markdownManager, UrlManager urlManager) {
		this.mailManager = mailManager;
		this.markdownManager = markdownManager;
		this.urlManager = urlManager;
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getRequest() == null) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			String rendered = markdownManager.render(markdown);
			Collection<User> mentionUsers = new MentionParser().parseMentions(rendered);
			if (!mentionUsers.isEmpty()) {
				String url;
				if (event instanceof CodeCommentCreated)
					url = urlManager.urlFor(((CodeCommentCreated)event).getComment(), null);
				else if (event instanceof CodeCommentReplied)
					url = urlManager.urlFor(((CodeCommentReplied)event).getReply(), null);
				else 
					url = null;
				
				if (url != null) {
					String subject = String.format("You are mentioned in a code comment on file '%s'", 
							event.getComment().getMarkPos().getPath());
					String body = String.format("Visit <a href='%s'>%s</a> for details", url, url);
					mailManager.sendMailAsync(mentionUsers.stream().map(User::getEmail).collect(Collectors.toList()), 
							subject, body);
				}
			}
		}
	}
	
}
