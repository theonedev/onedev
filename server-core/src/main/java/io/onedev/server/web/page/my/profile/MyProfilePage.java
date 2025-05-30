package io.onedev.server.web.page.my.profile;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.profileedit.ProfileEditPanel;
import io.onedev.server.web.page.my.MyPage;

public class MyProfilePage extends MyPage {

	public MyProfilePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ProfileEditPanel("content", new AbstractReadOnlyModel<>() {

			@Override
			public User getObject() {
				return getUser();
			}

		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My Profile"));
	}

}
