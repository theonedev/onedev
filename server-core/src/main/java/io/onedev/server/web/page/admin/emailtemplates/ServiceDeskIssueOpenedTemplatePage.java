package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;
import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_SERVICE_DESK_ISSUE_OPENED;
import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_SERVICE_DESK_ISSUE_OPENED;

@SuppressWarnings("serial")
public class ServiceDeskIssueOpenedTemplatePage extends AbstractTemplatePage {

	public ServiceDeskIssueOpenedTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_SERVICE_DESK_ISSUE_OPENED;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_SERVICE_DESK_ISSUE_OPENED;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of feedback email when issue is opened " +
				"via service desk";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("issue", 
				"represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> being opened via service desk");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Service Desk Issue Opened Template");
	}

}