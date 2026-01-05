package io.onedev.server.notification;

import static io.onedev.server.notification.NotificationUtils.getEmailBody;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.ai.AiTask;
import io.onedev.server.ai.responsehandlers.AddCodeCommentReply;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.codecomment.CodeCommentEdited;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.mail.MailService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityComment;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.CodeCommentMentionService;
import io.onedev.server.service.UserService;
import io.onedev.server.util.commenttext.MarkdownText;

@Singleton
public class CodeCommentNotificationManager {

	private static final int AI_TASK_TIMEOUT_SECONDS = 300;

	@Inject
	private MailService mailService;

	@Inject
	private UserService userService;

	@Inject
	private CodeCommentMentionService mentionService;

	@Transactional
	@Listen
	public void on(CodeCommentEvent event) {
		CodeComment comment = event.getComment();
		if (comment.getCompareContext().getPullRequest() == null && event.getUser().getType() != User.Type.SERVICE) {
			MarkdownText markdown = (MarkdownText) event.getCommentText();

			Collection<User> notifyUsers = new HashSet<>();

			notifyUsers.add(event.getUser());
			notifyUsers.add(comment.getUser());
			notifyUsers.addAll(comment.getReplies().stream().map(EntityComment::getUser).collect(toSet()));
			notifyUsers.addAll(comment.getChanges().stream().map(CodeCommentStatusChange::getUser).collect(toSet()));

			if (markdown != null) {
				for (String userName : new MentionParser().parseMentions(markdown.getRendered())) {
					User mentionedUser = userService.findByName(userName);
					if (mentionedUser != null) {
						mentionService.mention(comment, mentionedUser);
						if (mentionedUser.getType() == User.Type.AI) {
							if (!(event instanceof CodeCommentEdited) && isAiEntitled(event.getUser(), comment, mentionedUser)) {
								String systemPrompt = """
									You are mentioned in a code comment. The content mentioning you is presented as user prompt. \
									Use existing replies as conversation context. Call relevant tools to get information about \
									the code comment if necessary.""";
								var task = new AiTask(
									systemPrompt.formatted(mentionedUser.getName()), 
									event.getTextBody(), 
									comment.getTools(), 
									new AddCodeCommentReply(comment.getId()));
								userService.execute(mentionedUser, task, AI_TASK_TIMEOUT_SECONDS);						
							}
						} else {
							notifyUsers.add(mentionedUser);
						}
					}
				}
			}

			Set<String> emailAddresses = notifyUsers.stream()
					.filter(it -> it.getId() > 0
							&& it.getType() != User.Type.AI
							&& (!it.equals(event.getUser()) || it.isNotifyOwnEvents())
							&& it.getPrimaryEmailAddress() != null
							&& it.getPrimaryEmailAddress().isVerified())
					.map(it -> it.getPrimaryEmailAddress().getValue())
					.collect(toSet());

			if (!emailAddresses.isEmpty() && !(event instanceof CodeCommentEdited)) {
				String url = event.getUrl();
				String subject = String.format(
						"[Code Comment %s:%s] %s",
						event.getProject().getPath(), 
						comment.getMark().getPath(),
						StringUtils.capitalize(event.getActivity()));

				String summary = String.format(
						"%s %s", 
						event.getUser().getDisplayName(), event.getActivity());

				String threadingReferences = "<" + comment.getProject().getPath()
						+ "-codecomment-" + comment.getId() + "@onedev>";

				mailService.sendMailAsync(emailAddresses, Lists.newArrayList(),
						Lists.newArrayList(), subject,
						getEmailBody(true, event, summary, markdown != null ? markdown.getProcessed() : null, url, false, null),
						getEmailBody(false, event, summary, markdown != null ? markdown.getContent() : null, url, false, null),
						null, event.getUser().getDisplayName(), threadingReferences);
			}
		}
	}

	private boolean isAiEntitled(@Nullable User user, CodeComment comment, User ai) {
		if (user != null && user.getId() > 0) {
			if (user.isEntitledToAi(ai)) {
				return true;
			} else {
				new AddCodeCommentReply(comment.getId()).onResponse(user, "@%s sorry but you are not entitled to access me".formatted(user.getName()));				
				return false;
			}
		} else {
			if (comment.getProject().isEntitledToAi(ai)) {
				return true;
			} else {
				new AddCodeCommentReply(comment.getId()).onResponse(ai, "Sorry but this project is not entitled to access me");				
				return false;
			}
		}
	}

}
