package io.onedev.server.web.page.my.profile;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.util.WicketUtils;

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
		
		if (getUser().isServiceAccount()) {
			if (WicketUtils.isSubscriptionActive())
				add(new Fragment("note", "serviceAccountFrag", this));
			else
				add(new Fragment("note", "invalidServiceAccountFrag", this));
		} else if (getUser().getPassword() != null) {
			add(new Fragment("note", "authViaInternalDatabaseFrag", this));
		} else {
			add(new Fragment("note", "authViaExternalSystemFrag", this));
		}
		
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
