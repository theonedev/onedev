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
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.codecomment.CodeCommentUpdated;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class CodeCommentNotificationManager extends AbstractNotificationManager {
	
	private final MailManager mailManager;
	
	private final UserManager userManager;
	
	private final TransactionManager transactionManager;
	
	private final Dao dao;
	
	@Inject
	public CodeCommentNotificationManager(MailManager mailManager, MarkdownManager markdownManager, 
			UserManager userManager, SettingManager settingManager, 
			TransactionManager transactionManager, Dao dao) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.userManager = userManager;
		this.transactionManager = transactionManager;
		this.dao = dao;
	}
	
	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		transactionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				CodeCommentEvent clone = (CodeCommentEvent) event.cloneIn(dao);
				CodeComment comment = clone.getComment();
				if (comment.getCompareContext().getPullRequest() == null) {
					String markdown = clone.getMarkdown();
					String renderedMarkdown = markdownManager.render(markdown);
					String processedMarkdown = markdownManager.process(renderedMarkdown, clone.getProject(), null, null, true);
					
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
									&& !it.equals(clone.getUser()) 
									&& it.getPrimaryEmailAddress() != null 
									&& it.getPrimaryEmailAddress().isVerified())
							.map(it->it.getPrimaryEmailAddress().getValue())
							.collect(Collectors.toSet());

					if (!emailAddresses.isEmpty() && !(clone instanceof CodeCommentUpdated)) {
						String url = clone.getUrl();
						String subject = String.format("[Code Comment] %s:%s", 
								clone.getProject().getPath(), comment.getMark().getPath());
						
						String summary = String.format("%s %s code comment", 
								clone.getUser().getDisplayName(), clone.getActivity());
						
						String threadingReferences = "<" + comment.getProject().getPath() 
								+ "-codecomment-" + comment.getId() + "@onedev>";

						mailManager.sendMailAsync(emailAddresses, Lists.newArrayList(), 
								Lists.newArrayList(), subject, 
								getHtmlBody(clone, summary, processedMarkdown, url, false, null), 
								getTextBody(clone, summary, markdown, url, false, null), 
								null, threadingReferences);
					}
				}				
			}
			
		});
	}

}
