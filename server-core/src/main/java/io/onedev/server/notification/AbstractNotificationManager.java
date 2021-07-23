package io.onedev.server.notification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.codehaus.groovy.control.CompilationFailedException;

import groovy.text.SimpleTemplateEngine;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Event;
import io.onedev.server.event.MarkdownAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.support.administration.notificationtemplate.NotificationTemplateSetting;
import io.onedev.server.util.markdown.MarkdownManager;

public abstract class AbstractNotificationManager {

	protected final MarkdownManager markdownManager;
	
	protected final SettingManager settingManager;
	
	public AbstractNotificationManager(MarkdownManager markdownManager, SettingManager settingManager) {
		this.markdownManager = markdownManager;
		this.settingManager = settingManager;
	}
	
	protected String getHtmlBody(Event event, String url, @Nullable Unsubscribable unsubscribable) {
		String eventBody = null;
		if (event instanceof MarkdownAware) {
			eventBody = ((MarkdownAware) event).getMarkdown();
			if (eventBody != null) {
				Project project = null;
				if (event instanceof ProjectEvent)
					project = ((ProjectEvent) event).getProject();
				eventBody = markdownManager.process(markdownManager.render(eventBody), project, null, true);
			}
		}
		
		String template = null;
		
		Map<String, Object> bindings = new HashMap<>();
		
		bindings.put("event", event);
		bindings.put("eventBody", eventBody);
		bindings.put("unsubscribable", unsubscribable);
		bindings.put("eventUrl", url);
		
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
			String htmlBody = new SimpleTemplateEngine().createTemplate(template).make(bindings).toString();
			return htmlBody;
		} catch (CompilationFailedException | ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected String getTextBody(Event event, String url, @Nullable Unsubscribable unsubscribable) {
		String textBody = null;
		if (event instanceof MarkdownAware) {
			String markdown = ((MarkdownAware) event).getMarkdown();
			if (markdown != null) {
				textBody = String.format(""
						+ "%s"
						+ "\n"
						+ "Visit %s for details",
						markdown, url);
			}
		}
		if (textBody == null)
			textBody = String.format("Visit %s for details", url);
		if (unsubscribable != null) {
			textBody += "\n\n---------------------------------------------\nYou received this as you "
					+ "are participating or participated previously in this topic. ";
			if (unsubscribable.getEmailAddress() != null) {
				textBody += String.format("Mail to %s with any content to unsubscribe", 
						unsubscribable.getEmailAddress());
			} else {
				textBody += "To stop receiving notifications of this topic, please visit detail link above and unwatch it";
			}
		}
		return textBody;
	}
	
}
