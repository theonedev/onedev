package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_PULL_REQUEST_NOTIFICATION;

@SuppressWarnings("serial")
public class PullRequestNotificationTemplatePage extends AbstractNotificationTemplatePage {

	public PullRequestNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PULL_REQUEST_NOTIFICATION;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of various pull request notification emails";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("pullRequest", 
				"represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/PullRequest.java' target='_blank'>pull request</a> object to be notified");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Pull Request Notification Template");
	}

}