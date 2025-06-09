package io.onedev.server.web.page.admin.emailtemplates;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_SERVICE_DESK_ISSUE_OPEN_FAILED;
import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_SERVICE_DESK_ISSUE_OPEN_FAILED;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.CollectionUtils;

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
		return MessageFormat.format(_T("A {0} used as body of feedback email when failed to open issue via service desk"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("exception", 
				_T("represents the exception encountered when open issue via service desk"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Service Desk Issue Open Failed Template"));
	}

}