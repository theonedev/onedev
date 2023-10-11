package io.onedev.server.model.support.administration.emailtemplates;

import com.google.common.io.Resources;
import io.onedev.server.annotation.Code;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.util.GroovyUtils;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Editable
public class EmailTemplates implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_NOTIFICATION;

	public static final String DEFAULT_ISSUE_NOTIFICATION_UNSUBSCRIBED;

	public static final String DEFAULT_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
	
	public static final String DEFAULT_SERVICE_DESK_ISSUE_OPENED;

	public static final String DEFAULT_SERVICE_DESK_ISSUE_OPEN_FAILED;
	
	public static final String DEFAULT_USER_INVITATION;

	public static final String DEFAULT_EMAIL_VERIFICATION;
	
	public static final String DEFAULT_PASSWORD_RESET;

	public static final String DEFAULT_STOPWATCH_OVERDUE;
	
	public static final String DEFAULT_ALERT;
	
	public static final String PROP_ISSUE_NOTIFICATION = "issueNotification";
	
	public static final String PROP_PULL_REQUEST_NOTIFICATION = "pullRequestNotification";

	public static final String PROP_ISSUE_NOTIFICATION_UNSUBSCRIBED = "issueNotificationUnsubscribed";
	
	public static final String PROP_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED = "pullRequestNotificationUnsubscribed";
	
	public static final String PROP_SERVICE_DESK_ISSUE_OPENED = "serviceDeskIssueOpened";

	public static final String PROP_SERVICE_DESK_ISSUE_OPEN_FAILED = "serviceDeskIssueOpenFailed";
	
	public static final String PROP_USER_INVITATION = "userInvitation";

	public static final String PROP_EMAIL_VERIFICATION = "emailVerification";
	
	public static final String PROP_PASSWORD_RESET = "passwordReset";

	public static final String PROP_STOPWATCH_OVERDUE = "stopwatchOverdue";
	
	public static final String PROP_ALERT = "alert";
	
	static {
		try {
			URL url = Resources.getResource(EmailTemplates.class, "default-notification.tpl");
			DEFAULT_NOTIFICATION = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-service-desk-issue-opened.tpl");
			DEFAULT_SERVICE_DESK_ISSUE_OPENED = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-service-desk-issue-open-failed.tpl");
			DEFAULT_SERVICE_DESK_ISSUE_OPEN_FAILED = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-issue-notification-unsubscribed.tpl");
			DEFAULT_ISSUE_NOTIFICATION_UNSUBSCRIBED = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-pull-request-notification-unsubscribed.tpl");
			DEFAULT_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-user-invitation.tpl");
			DEFAULT_USER_INVITATION = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-email-verification.tpl");
			DEFAULT_EMAIL_VERIFICATION = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-password-reset.tpl");
			DEFAULT_PASSWORD_RESET = Resources.toString(url, StandardCharsets.UTF_8);

			url = Resources.getResource(EmailTemplates.class, "default-stopwatch-overdue.tpl");
			DEFAULT_STOPWATCH_OVERDUE = Resources.toString(url, StandardCharsets.UTF_8);
			
			url = Resources.getResource(EmailTemplates.class, "default-alert.tpl");
			DEFAULT_ALERT = Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String issueNotification = DEFAULT_NOTIFICATION;

	private String pullRequestNotification = DEFAULT_NOTIFICATION;
	
	private String issueNotificationUnsubscribed = DEFAULT_ISSUE_NOTIFICATION_UNSUBSCRIBED;
	
	private String pullRequestNotificationUnsubscribed = DEFAULT_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
	
	private String serviceDeskIssueOpened = DEFAULT_SERVICE_DESK_ISSUE_OPENED;
	
	private String serviceDeskIssueOpenFailed = DEFAULT_SERVICE_DESK_ISSUE_OPEN_FAILED;
	
	private String userInvitation = DEFAULT_USER_INVITATION;
	
	private String emailVerification = DEFAULT_EMAIL_VERIFICATION;
	
	private String passwordReset = DEFAULT_PASSWORD_RESET;
	
	private String stopwatchOverdue = DEFAULT_STOPWATCH_OVERDUE;
	private String alert = DEFAULT_ALERT;
	
	@Editable(order=200)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getIssueNotification() {
		return issueNotification;
	}

	public void setIssueNotification(String issueNotification) {
		this.issueNotification = issueNotification;
	}

	@Editable(order=300)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getPullRequestNotification() {
		return pullRequestNotification;
	}

	public void setPullRequestNotification(String pullRequestNotification) {
		this.pullRequestNotification = pullRequestNotification;
	}

	@Editable(order=310)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getIssueNotificationUnsubscribed() {
		return issueNotificationUnsubscribed;
	}

	public void setIssueNotificationUnsubscribed(String issueNotificationUnsubscribed) {
		this.issueNotificationUnsubscribed = issueNotificationUnsubscribed;
	}

	@Editable(order=320)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getPullRequestNotificationUnsubscribed() {
		return pullRequestNotificationUnsubscribed;
	}

	public void setPullRequestNotificationUnsubscribed(String pullRequestNotificationUnsubscribed) {
		this.pullRequestNotificationUnsubscribed = pullRequestNotificationUnsubscribed;
	}
	
	@Editable(order=400)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getServiceDeskIssueOpened() {
		return serviceDeskIssueOpened;
	}

	public void setServiceDeskIssueOpened(String serviceDeskIssueOpened) {
		this.serviceDeskIssueOpened = serviceDeskIssueOpened;
	}

	@Editable(order=500)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getServiceDeskIssueOpenFailed() {
		return serviceDeskIssueOpenFailed;
	}

	public void setServiceDeskIssueOpenFailed(String serviceDeskIssueOpenFailed) {
		this.serviceDeskIssueOpenFailed = serviceDeskIssueOpenFailed;
	}

	@Editable(order=600)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getUserInvitation() {
		return userInvitation;
	}

	public void setUserInvitation(String userInvitation) {
		this.userInvitation = userInvitation;
	}

	@Editable(order=700)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getEmailVerification() {
		return emailVerification;
	}

	public void setEmailVerification(String emailVerification) {
		this.emailVerification = emailVerification;
	}

	@Editable(order=800)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getPasswordReset() {
		return passwordReset;
	}

	public void setPasswordReset(String passwordReset) {
		this.passwordReset = passwordReset;
	}

	@Editable(order=850)
	@Code(language = Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getStopwatchOverdue() {
		return stopwatchOverdue;
	}

	public void setStopwatchOverdue(String stopwatchOverdue) {
		this.stopwatchOverdue = stopwatchOverdue;
	}

	@Editable(order=900)
	@Code(language=Code.GROOVY_TEMPLATE)
	@OmitName
	@NotEmpty
	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}
	
	public static String evalTemplate(boolean htmlVersion, String template, Map<String, Object> bindings) {
		var currentBindins = new HashMap<String, Object>(bindings);	
		currentBindins.put("htmlVersion", htmlVersion);
		return GroovyUtils.evalTemplate(template, currentBindins);
	}
	
}
