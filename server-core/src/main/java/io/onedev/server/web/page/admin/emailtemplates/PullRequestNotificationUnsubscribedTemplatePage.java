package io.onedev.server.web.page.admin.emailtemplates;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.CollectionUtils;

public class PullRequestNotificationUnsubscribedTemplatePage extends AbstractTemplatePage {

	public PullRequestNotificationUnsubscribedTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_PULL_REQUEST_NOTIFICATION_UNSUBSCRIBED;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of feedback email when unsubscribed from pull request notification"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("pullRequest", 
				_T("represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a>"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Pull Request Notification Unsubscribed Template"));
	}

}