package io.onedev.server.web.page.my.preferences;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.preferences.PreferencesEditPanel;
import io.onedev.server.web.page.my.MyPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class MyPreferencesPage extends MyPage {

	public MyPreferencesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new PreferencesEditPanel("content", new AbstractReadOnlyModel<>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}

		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My Preferences");
	}

}
