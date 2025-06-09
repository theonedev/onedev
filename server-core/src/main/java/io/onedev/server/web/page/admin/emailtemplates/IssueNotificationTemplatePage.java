package io.onedev.server.web.page.admin.emailtemplates;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_ISSUE_NOTIFICATION;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.CollectionUtils;

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
		return MessageFormat.format(_T("A {0} used as body of various issue notification emails"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("issue", 
				_T("represents the <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/Issue.java' target='_blank'>issue</a> object to be notified"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Issue Notification Template"));
	}

}