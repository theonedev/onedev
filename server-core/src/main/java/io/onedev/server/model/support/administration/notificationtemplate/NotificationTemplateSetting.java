package io.onedev.server.model.support.administration.notificationtemplate;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import com.google.common.io.Resources;

import io.onedev.server.util.validation.annotation.Code;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
public class NotificationTemplateSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final List<String> DEFAULT_TEMPLATE;
	
	public static final String PROP_ISSUE_NOTIFICATION_TEMPLATE = "issueNotificationTemplate";
	
	public static final String PROP_PULL_REQUEST_NOTIFICATION_TEMPLATE = "pullRequestNotificationTemplate";
	
	public static final String COMMON_HELP = "Notification template is a "
			+ "<a href='https://docs.groovy-lang.org/latest/html/api/groovy/text/SimpleTemplateEngine.html' target='_blank'>Groovy simple template</a>. "
			+ "When evaluating this template, below variables will be available:"
			+ "<ul class='mb-0'>"
			+ "<li><code>event:</code> <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification"
			+ "<li><code>eventSummary:</code> a string representing summary of the event"
			+ "<li><code>eventBody:</code> a string representing body of the event. May be <code>null</code>"
			+ "<li><code>eventUrl:</code> a string representing event detail url"
			+ "<li><code>replyable:</code> a boolean indiciating whether or not topic comment can be created directly by replying the email"
			+ "<li><code>unsubscribable:</code> an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information. "
			+ "		A <code>null</code> value means that the notification can not be unsubscribed";
	
	static {
		URL url = Resources.getResource(NotificationTemplateSetting.class, "default-notification-template.html");
		try {
			DEFAULT_TEMPLATE = Resources.readLines(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<String> issueNotificationTemplate = DEFAULT_TEMPLATE;

	private List<String> pullRequestNotificationTemplate = DEFAULT_TEMPLATE;
	
	@Editable(order=200)
	@Code(language=Code.HTML_TEMPLATE)
	@OmitName
	@Size(min=1, message="May not be empty")
	public List<String> getIssueNotificationTemplate() {
		return issueNotificationTemplate;
	}

	public void setIssueNotificationTemplate(List<String> issueNotificationTemplate) {
		this.issueNotificationTemplate = issueNotificationTemplate;
	}

	@Editable(order=300)
	@Code(language=Code.HTML_TEMPLATE)
	@OmitName
	@Size(min=1, message="May not be empty")
	public List<String> getPullRequestNotificationTemplate() {
		return pullRequestNotificationTemplate;
	}

	public void setPullRequestNotificationTemplate(List<String> pullRequestNotificationTemplate) {
		this.pullRequestNotificationTemplate = pullRequestNotificationTemplate;
	}
	
	public static String getTemplateHelp(Map<String, String> variableHelp) {
		StringBuilder builder = new StringBuilder(COMMON_HELP);
		for (Map.Entry<String, String> entry: variableHelp.entrySet())
			builder.append("<li><code>" + entry.getKey() + ":</code> " + entry.getValue());
		return builder.toString();
	}
	
}
