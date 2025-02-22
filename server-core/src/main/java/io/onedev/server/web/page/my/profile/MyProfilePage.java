package io.onedev.server.web.page.my.profile;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.page.my.MyPage;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class MyProfilePage extends MyPage {

	public MyProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getUser().getPassword() != null)
			add(new Fragment("authSource", "authViaInternalDatabaseFrag", this));
		else
			add(new Fragment("authSource", "authViaExternalSystemFrag", this));
		
		add(new ProfileEditPanel("content", new AbstractReadOnlyModel<>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}

		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "My Profile");
	}

}
