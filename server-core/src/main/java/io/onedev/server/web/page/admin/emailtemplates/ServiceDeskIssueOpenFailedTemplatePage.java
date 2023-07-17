package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;
import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
public class ServiceDeskIssueOpenFailedTemplatePage extends AbstractTemplatePage {

	public ServiceDeskIssueOpenFailedTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_SERVICE_DESK_ISSUE_OPEN_FAILED;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_SERVICE_DESK_ISSUE_OPEN_FAILED;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of feedback email when failed to open issue " +
				"via service desk";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("exception", 
				"represents the exception encountered when open issue via service desk");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Service Desk Issue Open Failed Template");
	}

}