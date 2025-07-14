package io.onedev.server.web.page.my.twofactorauthentication;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.User;
import io.onedev.server.web.component.user.twofactorauthentication.TwoFactorAuthenticationStatusPanel;
import io.onedev.server.web.page.my.MyPage;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MyTwoFactorAuthenticationPage extends MyPage {

	public MyTwoFactorAuthenticationPage(PageParameters params) {
		super(params);
		if (getUser().isServiceAccount() || getUser().isDisabled())
			throw new IllegalStateException();
		else if (!getUser().isEnforce2FA())
			throw new ExplicitException(_T("Two-factor authentication not enabled"));		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new TwoFactorAuthenticationStatusPanel("content") {
			@Override
			protected User getUser() {
				return MyTwoFactorAuthenticationPage.this.getUser();
			}
		});
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Two Factor Authentication"));
	}

}
