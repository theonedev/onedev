package io.onedev.server.notification;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.markdown.MarkdownManager;
import io.onedev.server.util.markdown.MentionParser;

@Singleton
public class CodeCommentNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final MarkdownManager markdownManager;
	
	private final UrlManager urlManager;
	
	private final UserManager userManager;
	
	@Inject
	public CodeCommentNotificationManager(MailManager mailManager, MarkdownManager markdownManager, 
			UrlManager urlManager, UserManager userManager) {
		this.mailManager = mailManager;
		this.markdownManager = markdownManager;
		this.urlManager = urlManager;
		this.userManager = userManager;
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getComment().getRequest() == null) {
			MarkdownAware markdownAware = (MarkdownAware) event;
			String markdown = markdownAware.getMarkdown();
			String rendered = markdownManager.render(markdown);
			
			for (String userName: new MentionParser().parseMentions(rendered)) {
				User user = userManager.findByName(userName);
				if (user != null) { 
					String url;
					if (event instanceof CodeCommentCreated)
						url = urlManager.urlFor(((CodeCommentCreated)event).getComment(), null);
					else if (event instanceof CodeCommentReplied)
						url = urlManager.urlFor(((CodeCommentReplied)event).getReply(), null);
					else 
						url = null;
					
					if (url != null) {
						String subject = String.format("You are mentioned in a code comment on file '%s'", 
								event.getComment().getMark().getPath());
						mailManager.sendMailAsync(Sets.newHashSet(user.getEmail()), subject, 
								getHtmlBody(event, url), getTextBody(event, url));
					}
				}
			}
		}
	}
	
}
