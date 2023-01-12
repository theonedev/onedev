package io.onedev.server.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

import io.onedev.server.entitymanager.CodeCommentMentionManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.codecomment.CodeCommentUpdated;
import io.onedev.server.mail.MailManager;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.commenttext.MarkdownText;

@Singleton
public class CodeCommentNotificationManager extends AbstractNotificationManager {

	private final MailManager mailManager;

	private final UserManager userManager;

	private final TransactionManager transactionManager;

	private final CodeCommentMentionManager mentionManager;
	
	@Inject
	public CodeCommentNotificationManager(MailManager mailManager, MarkdownManager markdownManager,
										  UserManager userManager, SettingManager settingManager,
										  TransactionManager transactionManager, 
										  CodeCommentMentionManager mentionManager) {
		super(markdownManager, settingManager);
		this.mailManager = mailManager;
		this.userManager = userManager;
		this.transactionManager = transactionManager;
		this.mentionManager = mentionManager;
	}

	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		transactionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				CodeComment comment = event.getComment();
				if (comment.getCompareContext().getPullRequest() == null) {
					MarkdownText markdown = (MarkdownText) event.getCommentText();

					Collection<User> notifyUsers = new HashSet<>();

					notifyUsers.add(comment.getUser());
					notifyUsers.addAll(comment.getReplies().stream().map(it -> it.getUser()).collect(Collectors.toSet()));
					notifyUsers.addAll(comment.getChanges().stream().map(it -> it.getUser()).collect(Collectors.toSet()));

					if (markdown != null) {
						for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
							User user = userManager.findByName(userName);
							if (user != null) {
								mentionManager.mention(comment, user);
								notifyUsers.add(user);
							}
						}
					}

					Set<String> emailAddresses = notifyUsers.stream()
							.filter(it -> it.isOrdinary()
									&& !it.equals(event.getUser())
									&& it.getPrimaryEmailAddress() != null
									&& it.getPrimaryEmailAddress().isVerified())
							.map(it -> it.getPrimaryEmailAddress().getValue())
							.collect(Collectors.toSet());

					if (!emailAddresses.isEmpty() && !(event instanceof CodeCommentUpdated)) {
						String url = event.getUrl();
						String subject = String.format("[Code Comment] %s:%s",
								event.getProject().getPath(), comment.getMark().getPath());

						String summary = String.format("%s %s code comment",
								event.getUser().getDisplayName(), event.getActivity());

						String threadingReferences = "<" + comment.getProject().getPath()
								+ "-codecomment-" + comment.getId() + "@onedev>";

						mailManager.sendMailAsync(emailAddresses, Lists.newArrayList(),
								Lists.newArrayList(), subject,
								getHtmlBody(event, summary, markdown != null ? markdown.getProcessed() : null, url, false, null),
								getTextBody(event, summary, markdown != null ? markdown.getContent() : null, url, false, null),
								null, threadingReferences);
					}
				}
			}

		});
	}

}
