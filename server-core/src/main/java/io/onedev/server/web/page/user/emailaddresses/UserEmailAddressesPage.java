package io.onedev.server.web.page.user.emailaddresses;

import static io.onedev.server.model.User.Type.ORDINARY;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.emailaddresses.EmailAddressesPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserEmailAddressesPage extends UserPage {

	public UserEmailAddressesPage(PageParameters params) {
		super(params);
		if (getUser().getType() != ORDINARY)
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

}
