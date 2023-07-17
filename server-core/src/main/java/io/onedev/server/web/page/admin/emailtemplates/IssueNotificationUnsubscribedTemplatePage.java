package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;
import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
public class IssueNotificationUnsubscribedTemplatePage extends AbstractTemplatePage {

	public IssueNotificationUnsubscribedTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_ISSUE_NOTIFICATION_UNSUBSCRIBED;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_ISSUE_NOTIFICATION_UNSUBSCRIBED;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of feedback email when unsubscribed " +
				"from issue notification";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("issue", 
				"represents the unsubscribed <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a>");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue Notification Unsubscribed Template");
	}

}