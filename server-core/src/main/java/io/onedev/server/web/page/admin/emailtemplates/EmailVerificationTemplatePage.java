package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
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
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of address verification email";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"user", "<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to verify email for",
				"emailAddress", "Email address to verify", 
				"serverUrl", "root url of OneDev server",
				"verificationUrl", "url following which to verify email address");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Email Verification Template");
	}

}