package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.Map;

import static io.onedev.server.model.support.administration.emailtemplates.EmailTemplates.*;

@SuppressWarnings("serial")
public class UserInvitationTemplatePage extends AbstractTemplatePage {

	public UserInvitationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPropertyName() {
		return PROP_USER_INVITATION;
	}

	@Override
	protected String getDefaultTemplate() {
		return DEFAULT_USER_INVITATION;
	}

	@Override
	protected String getHelpText() {
		return "A " + GROOVY_TEMPLATE_LINK + " used as body of user invitation email";
	}

	@Override
	protected Map<String, String> getVariableHelp() {
		return CollectionUtils.newLinkedHashMap("setupAccountUrl", "the url to set up user account");
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "User Invitation Template");
	}

}