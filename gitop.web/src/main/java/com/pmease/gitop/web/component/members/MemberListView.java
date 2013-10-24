package com.pmease.gitop.web.component.members;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class MemberListView extends Panel {

	public MemberListView(String id, IModel<List<User>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		@SuppressWarnings("unchecked")
		ListView<User> membersView = new ListView<User>("member", (IModel<List<User>>) getDefaultModel()) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User user = item.getModelObject();
				item.add(new AvatarImage("avatar", item.getModel()));
				AbstractLink link = PageSpec.newUserHomeLink("userlink", user);
				item.add(link);
				link.add(new Label("name", Model.of(user.getName())));
				item.add(new Label("displayName", Model.of(user.getDisplayName())));
				item.add(createActionsPanel("actions", item.getModel()));
			}
			
		};
		
		add(membersView);
	}
	
	protected Component createActionsPanel(String id, IModel<User> model) {
		return new WebMarkupContainer(id).setVisibilityAllowed(false);
	}
}
