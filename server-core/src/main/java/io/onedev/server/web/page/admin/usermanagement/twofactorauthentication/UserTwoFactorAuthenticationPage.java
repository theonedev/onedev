package io.onedev.server.web.page.admin.usermanagement.twofactorauthentication;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.page.admin.usermanagement.UserPage;

@SuppressWarnings("serial")
public class UserTwoFactorAuthenticationPage extends UserPage {

	public UserTwoFactorAuthenticationPage(PageParameters params) {
		super(params);
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getUser().getSsoConnector() != null) {
			add(new Label("content", "This account is currently authenticated via SSO provider '" 
					+ getUser().getSsoConnector() + "', "
					+ "and two-factor authentication should be configured there")
					.add(AttributeAppender.append("class", "alert alert-light-warning alert-notice mb-0")));
		} else if (getUser().getTwoFactorAuthentication() != null) {
			Fragment fragment = new Fragment("content", "enabledFrag", this);
			fragment.add(new AjaxLink<Void>("disable") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(
							"Do you really want to disable two-factor authentication for this account?"));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					getUser().setTwoFactorAuthentication(null);
					getUserManager().update(getUser(), null);
					Session.get().success("Two-factor authentication disabled");
					setResponsePage(UserTwoFactorAuthenticationPage.class, UserTwoFactorAuthenticationPage.paramsOf(getUser()));
				}
				
			});
			add(fragment);
		} else {
			add(new Fragment("content", "disabledFrag", this));
		}
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Two Factor Authentication");
	}

}
