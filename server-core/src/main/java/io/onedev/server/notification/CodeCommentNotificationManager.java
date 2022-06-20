package io.onedev.server.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

import io.onedev.commons.loader.Listen;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.codecomment.CodeCommentCreated;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentReplied;
import io.onedev.server.event.codecomment.CodeCommentStatusChanged;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.CodeComment;
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
		CodeComment comment = event.getComment();
		if (comment.getCompareContext().getPullRequest() == null) {
			String markdown = event.getMarkdown();
			String renderedMarkdown = markdownManager.render(markdown);
			String processedMarkdown = markdownManager.process(renderedMarkdown, event.getProject(), null, null, true);
			
			Collection<User> notifyUsers = new HashSet<>(); 
			
			notifyUsers.add(comment.getUser());
			notifyUsers.addAll(comment.getReplies().stream().map(it->it.getUser()).collect(Collectors.toSet()));
			notifyUsers.addAll(comment.getChanges().stream().map(it->it.getUser()).collect(Collectors.toSet()));
			
			for (String userName: new MentionParser().parseMentions(renderedMarkdown)) {
				User user = userManager.findByName(userName);
				if (user != null) 
					notifyUsers.add(user);
			}
		
			Set<String> emailAddresses = notifyUsers.stream()
					.filter(it-> it.isOrdinary() 
							&& !it.equals(event.getUser()) 
							&& it.getPrimaryEmailAddress() != null 
							&& it.getPrimaryEmailAddress().isVerified())
					.map(it->it.getPrimaryEmailAddress().getValue())
					.collect(Collectors.toSet());

			if (!emailAddresses.isEmpty()) {
				String url;
				if (event instanceof CodeCommentCreated)
					url = urlManager.urlFor(comment);
				else if (event instanceof CodeCommentReplied)
					url = urlManager.urlFor(((CodeCommentReplied)event).getReply());
				else if (event instanceof CodeCommentStatusChanged)
					url = urlManager.urlFor(((CodeCommentStatusChanged)event).getChange());
				else 
					url = null;
				
				if (url != null) {
					String subject = String.format("[Code Comment] %s:%s", 
							event.getProject().getPath(), comment.getMark().getPath());
					
					String summary = String.format("%s %s code comment", 
							event.getUser().getDisplayName(), event.getActivity());
					
					String threadingReferences = "<" + comment.getProject().getPath() 
							+ "-codecomment-" + comment.getId() + "@onedev>";

					mailManager.sendMailAsync(emailAddresses, Lists.newArrayList(), 
							Lists.newArrayList(), subject, 
							getHtmlBody(event, summary, processedMarkdown, url, false, null), 
							getTextBody(event, summary, markdown, url, false, null), 
							null, threadingReferences);
				}
			}
		}
	}

}
