package io.onedev.server.notification;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Event;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.markdown.MarkdownManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNotificationManager {

	protected final MarkdownManager markdownManager;
	
	protected final SettingManager settingManager;
	
	public AbstractNotificationManager(MarkdownManager markdownManager, SettingManager settingManager) {
		this.markdownManager = markdownManager;
		this.settingManager = settingManager;
	}
	
	protected String getEmailBody(boolean htmlVersion, Event event, String eventSummary, @Nullable String eventBody,
								 String eventUrl, boolean replyable, @Nullable Unsubscribable unsubscribable) {
		String template = null;
		
		Map<String, Object> bindings = new HashMap<>();

		if (htmlVersion)
			eventSummary = HtmlEscape.escapeHtml5(eventSummary);
		
		bindings.put("event", event);
		bindings.put("eventSummary", eventSummary);
		bindings.put("eventBody", eventBody);
		bindings.put("eventUrl", eventUrl);
		bindings.put("replyable", replyable);
		bindings.put("unsubscribable", unsubscribable);
		
		if (event instanceof IssueEvent) { 
			template = settingManager.getEmailTemplates().getIssueNotification();
			bindings.put("issue", ((IssueEvent) event).getIssue());
		} else if (event instanceof PullRequestEvent) {
			template = settingManager.getEmailTemplates().getPullRequestNotification();
			bindings.put("pullRequest", ((PullRequestEvent) event).getRequest());
		}  
		if (template == null)
			template = EmailTemplates.DEFAULT_NOTIFICATION;
			
		return EmailTemplates.evalTemplate(htmlVersion, template, bindings);
	}
	
	protected boolean isNotified(Collection<String> notifiedEmailAddresses, User user) {
		for (EmailAddress emailAddress: user.getEmailAddresses()) {
			if (emailAddress.isVerified() && notifiedEmailAddresses.contains(emailAddress.getValue()))
				return true;
		}
		return false;
	}
	
}
