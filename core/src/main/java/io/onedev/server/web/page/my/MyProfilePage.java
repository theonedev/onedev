package io.onedev.server.web.page.my;

import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;

@SuppressWarnings("serial")
public class MyProfilePage extends MyPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ProfileEditPanel("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}
			
		}));
	}

}
