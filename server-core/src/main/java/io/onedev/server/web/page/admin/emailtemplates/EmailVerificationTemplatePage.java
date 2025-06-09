package io.onedev.server.web.page.admin.emailtemplates;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.DEFAULT_EMAIL_VERIFICATION;
import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.PROP_EMAIL_VERIFICATION;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.CollectionUtils;

public class EmailVerificationTemplatePage extends AbstractTemplatePage {

	public EmailVerificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_EMAIL_VERIFICATION;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_EMAIL_VERIFICATION;
	}

	@Override
	protected String getHelpText() {
		return MessageFormat.format(_T("A {0} used as body of address verification email"), GROOVY_TEMPLATE_LINK);
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"user", _T("<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for"),
				"emailAddress", _T("Email address to verify"), 
				"serverUrl", _T("root url of OneDev server"),
				"verificationUrl", _T("url following which to verify email address"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Email Verification Template"));
	}

}