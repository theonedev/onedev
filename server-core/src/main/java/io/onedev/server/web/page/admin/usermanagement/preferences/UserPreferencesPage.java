package io.onedev.server.web.page.admin.usermanagement.preferences;

import io.onedev.server.web.component.user.preferences.PreferencesEditPanel;
import io.onedev.server.web.page.admin.usermanagement.UserPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class UserPreferencesPage extends UserPage {

	public UserPreferencesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new PreferencesEditPanel("content", userModel));
	}

}
