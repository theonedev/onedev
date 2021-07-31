package io.onedev.server.notification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.codehaus.groovy.control.CompilationFailedException;
import org.unbescape.html.HtmlEscape;

import groovy.text.SimpleTemplateEngine;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Event;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;

public abstract class AbstractNotificationManager {

	protected final MarkdownManager markdownManager;
	
	protected final SettingManager settingManager;
	
	public AbstractNotificationManager(MarkdownManager markdownManager, SettingManager settingManager) {
		this.markdownManager = markdownManager;
		this.settingManager = settingManager;
	}
	
	protected String getHtmlBody(Event event, @Nullable String eventSummary, @Nullable String eventBody, 
			String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		String template = null;
		
		Map<String, Object> bindings = new HashMap<>();
		
		if (eventSummary != null)
			eventSummary = HtmlEscape.escapeHtml5(eventSummary);
		eventUrl = HtmlEscape.escapeHtml5(eventUrl);
		
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
		} else if (event instanceof EntityPersisted) {
			AbstractEntity entity = ((EntityPersisted) event).getEntity();
			if (entity instanceof PullRequestReview) {
				template = StringUtils.join(settingManager.getNotificationTemplateSetting().getPullRequestNotificationTemplate(), "\n");
				bindings.put("pullRequest", ((PullRequestReview) entity).getRequest());
			} else if (entity instanceof PullRequestAssignment) {
				template = StringUtils.join(settingManager.getNotificationTemplateSetting().getPullRequestNotificationTemplate(), "\n");
				bindings.put("pullRequest", ((PullRequestAssignment) entity).getRequest());
			}
		}  
		if (template == null)
			template = StringUtils.join(NotificationTemplateSetting.DEFAULT_TEMPLATE, "\n");
			
		try {
			return new SimpleTemplateEngine().createTemplate(template).make(bindings).toString();
		} catch (CompilationFailedException | ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String getTextBody(Event event, @Nullable String eventSummary, @Nullable String eventBody, 
			String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		StringBuilder textBody = new StringBuilder();
		if (eventSummary != null)
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
	
}
