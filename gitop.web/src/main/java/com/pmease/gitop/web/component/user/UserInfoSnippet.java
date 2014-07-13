package com.pmease.gitop.web.component.user;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.User;

@SuppressWarnings("serial")
public abstract class UserInfoSnippet extends Panel {

	public UserInfoSnippet(String id, IModel<User> userModel) {
		super(id, userModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<User> userModel = new AbstractReadOnlyModel<User>() {

			@Override
			public User getObject() {
				return (User) getDefaultModelObject();
			}
			
		};
		add(new UserLink("avatar", userModel, AvatarMode.AVATAR));
		add(new UserLink("name", userModel, AvatarMode.NAME));
		add(newInfoLine("info"));
	}

	protected abstract Component newInfoLine(String componentId);
	
}
