package io.onedev.server.web.page.my.emailaddresses;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.emailaddresses.EmailAddressesPanel;
import io.onedev.server.web.page.my.MyPage;

public class MyEmailAddressesPage extends MyPage {

	public MyEmailAddressesPage(PageParameters params) {
		super(params);
		if (getUser().isServiceAccount())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new EmailAddressesPanel("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getUser();
			}
			
		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My Email Addresses"));
	}

}
