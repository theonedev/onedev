package io.onedev.server.notification;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.codehaus.groovy.control.CompilationFailedException;
import org.unbescape.html.HtmlEscape;

import groovy.text.SimpleTemplateEngine;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;

public abstract class AbstractNotificationManager {

	protected final MarkdownManager markdownManager;
	
	protected final SettingManager settingManager;
	
	public AbstractNotificationManager(MarkdownManager markdownManager, SettingManager settingManager) {
		this.markdownManager = markdownManager;
		this.settingManager = settingManager;
	}
	
	protected String getHtmlBody(Object event, String eventSummary, @Nullable String eventBody, 
			String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		String template = null;
		
		Map<String, Object> bindings = new HashMap<>();
		
		eventSummary = HtmlEscape.escapeHtml4(eventSummary);
		eventUrl = HtmlEscape.escapeHtml4(eventUrl);
		
		bindings.put("event", event);
		bindings.put("eventSummary", eventSummary);
		bindings.put("eventBody", eventBody);
		bindings.put("eventUrl", eventUrl);
		bindings.put("replyable", replyable);
		bindings.put("unsubscribable", unsubscribable);
		
		if (event instanceof IssueEvent) { 
			template = StringUtils.join(settingManager.getNotificationTemplateSetting().getIssueNotificationTemplate(), "\n");
			bindings.put("issue", ((IssueEvent) event).getIssue());
		} else if (event instanceof PullRequestEvent) {
			template = StringUtils.join(settingManager.getNotificationTemplateSetting().getPullRequestNotificationTemplate(), "\n");
			bindings.put("pullRequest", ((PullRequestEvent) event).getRequest());
		}  
		if (template == null)
			template = StringUtils.join(NotificationTemplateSetting.DEFAULT_TEMPLATE, "\n");
			
		try {
			return new SimpleTemplateEngine().createTemplate(template).make(bindings).toString();
		} catch (CompilationFailedException | ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String getTextBody(Object event, String eventSummary, @Nullable String eventBody, 
			String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		StringBuilder textBody = new StringBuilder();
		textBody.append(eventSummary).append("\n\n");
		if (eventBody != null) 
			textBody.append(eventBody).append("\n\n");
		
		if (replyable)
			textBody.append("Reply this email to post comment, or visit " + eventUrl + " for details");
		else
			textBody.append("Visit " + eventUrl + " for details");
		
		if (unsubscribable != null) {
			textBody.append("\n\n---------------------------------------------\nYou received this notification as you "
					+ "are participating in this topic. ");
			if (unsubscribable.getEmailAddress() != null) {
				textBody.append(String.format("Mail to %s with any content to unsubscribe", 
						unsubscribable.getEmailAddress()));
			} else {
				textBody.append("To stop receiving notifications of this topic, please visit detail link above and unwatch it");
			}
		}
		return textBody.toString();
	}
	
	protected boolean isNotified(Collection<String> notifiedEmailAddresses, User user) {
		for (EmailAddress emailAddress: user.getEmailAddresses()) {
			if (emailAddress.isVerified() && notifiedEmailAddresses.contains(emailAddress.getValue()))
				return true;
		}
		return false;
	}
	
}
