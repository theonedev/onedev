package io.onedev.server.notification;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class CodeCommentNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final UrlManager urlManager;
	
	private final UserManager userManager;
	
	@Inject
	public CodeCommentNotificationManager(MailManager mailManager, MarkdownManager markdownManager, 
			UrlManager urlManager, UserManager userManager, SettingManager settingManager) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.urlManager = urlManager;
		this.userManager = userManager;
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getComment().getRequest() == null) {
			String markdown = event.getMarkdown();
			String renderedMarkdown = markdownManager.render(markdown);
			String processedMarkdown = markdownManager.process(renderedMarkdown, event.getProject(), null, true);
			
			for (String userName: new MentionParser().parseMentions(renderedMarkdown)) {
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
						String subject = String.format("[Code Comment] (Mentioned You) %s:%s", 
								event.getProject().getName(), event.getComment().getMark().getPath());
						String summary = String.format("%s added code comment", 
								event.getUser().getDisplayName());
						String threadingReferences = "<" + event.getComment().getProject().getName() 
								+ "-codecomment-" + event.getComment().getId() + "@onedev>";

						mailManager.sendMailAsync(Sets.newHashSet(user.getEmail()), Lists.newArrayList(), 
								Lists.newArrayList(), subject, 
								getHtmlBody(event, summary, processedMarkdown, url, false, null), 
								getTextBody(event, summary, markdown, url, false, null), 
								null, threadingReferences);
					}
				}
			}
		}
	}

}
