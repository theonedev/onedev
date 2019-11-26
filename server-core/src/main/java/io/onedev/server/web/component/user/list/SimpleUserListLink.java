package io.onedev.server.web.component.user.list;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;

import io.onedev.server.model.User;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class SimpleUserListLink extends DropdownLink {

	public SimpleUserListLink(String id) {
		super(id);
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new SimpleUserListPanel(id) {

			@Override
			protected List<User> getUsers() {
				return SimpleUserListLink.this.getUsers();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new SimpleUserListCssResourceReference()));
	}

	protected abstract List<User> getUsers();
	
}
