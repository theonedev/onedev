package io.onedev.server.web.component.user.list;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import io.onedev.server.model.User;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.page.admin.user.UserProfilePage;

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
			Link<Void> link = new BookmarkablePageLink<Void>(usersView.newChildId(), 
					UserProfilePage.class, UserProfilePage.paramsOf(user));
			link.add(new UserAvatar("avatar", UserIdent.of(user)));
			link.add(new Label("name", user.getDisplayName()));
			usersView.add(link);
		}
		add(usersView);
	}

	protected abstract List<User> getUsers();
}
