package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
public class PasswordResetTemplatePage extends AbstractTemplatePage {

	public PasswordResetTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_PASSWORD_RESET;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_PASSWORD_RESET;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of password reset email";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap(
				"user", "<a href=\"https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/model/User.java\">user</a> to reset password for",
				"passwordResetUrl", "url to reset password");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Password Reset Template");
	}

}