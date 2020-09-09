package io.onedev.server.web.page.my.avatar;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.avataredit.AvatarEditPanel;
import io.onedev.server.web.page.my.MyPage;

@SuppressWarnings("serial")
public class MyAvatarPage extends MyPage {
	
	public MyAvatarPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AvatarEditPanel("content", new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return getLoginUser();
			}
			
		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Edit My Avatar");
	}

}
