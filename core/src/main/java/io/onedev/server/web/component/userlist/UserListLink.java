package io.onedev.server.web.component.userlist;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.model.User;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class UserListLink extends DropdownLink {

	public UserListLink(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new UserListPanel(id) {

			@Override
			protected List<User> getUsers() {
				return UserListLink.this.getUsers();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserListCssResourceReference()));
	}

	protected abstract List<User> getUsers();
	
}
