package io.onedev.server.web.component.user.list;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.UserAvatar;

@SuppressWarnings("serial")
abstract class SimpleUserListPanel extends Panel {

	public SimpleUserListPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		RepeatingView usersView = new RepeatingView("users");
		for (User user:  getUsers()) {
			WebMarkupContainer container = new WebMarkupContainer(usersView.newChildId());
			container.add(new UserAvatar("avatar", user));
			container.add(new Label("name", user.getDisplayName()));
			usersView.add(container);
		}
		add(usersView);
	}

	protected abstract List<User> getUsers();
}
