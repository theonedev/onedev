package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_ISSUE_NOTIFICATION;

@SuppressWarnings("serial")
public class IssueNotificationTemplatePage extends AbstractNotificationTemplatePage {

	public IssueNotificationTemplatePage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected String getPropertyName() {
		return PROP_ISSUE_NOTIFICATION;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of various issue notification emails";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("issue", 
				"represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue Notification Template");
	}

}