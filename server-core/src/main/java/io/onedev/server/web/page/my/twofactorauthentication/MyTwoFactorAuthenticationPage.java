package io.onedev.server.web.page.my.twofactorauthentication;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.twofactorauthentication.TwoFactorAuthenticationSetupPanel;
import io.onedev.server.web.page.my.MyPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class MyTwoFactorAuthenticationPage extends MyPage {

	public MyTwoFactorAuthenticationPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!getLoginUser().isEnforce2FA())
			throw new ExplicitException("Two-factor authentication not enabled");
		if (getLoginUser().getSsoConnector() != null)
			throw new ExplicitException("Two-factor authentication not applicable as you are authenticating via SSO");

		add(new Label("notice", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (getLoginUser().getTwoFactorAuthentication() != null)
					return "Two-factor authentication already configured. You may reconfigure it below if desired";
				else
					return "Two-factor authentication is enabled. Please set it up below";
			}
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (getLoginUser().getTwoFactorAuthentication() != null)
					return "alert-light-success";
				else
					return "alert-light-warning";
			}
		})));
		add(new TwoFactorAuthenticationSetupPanel("setup") {

			@Override
			protected void onConfigured(AjaxRequestTarget target) {
				setResponsePage(MyTwoFactorAuthenticationPage.class);
			}

			@Override
			protected User getUser() {
				return getLoginUser();
			}

		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Two Factor Authentication");
	}

}
