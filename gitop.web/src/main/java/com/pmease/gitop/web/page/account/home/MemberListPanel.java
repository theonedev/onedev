package com.pmease.gitop.web.page.account.home;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.component.avatar.AvatarImage;
import com.pmease.gitop.web.model.UserModel;
import com.pmease.gitop.web.page.PageSpec;

@SuppressWarnings("serial")
public class MemberListPanel extends Panel {

	public MemberListPanel(String id, IModel<User> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<User>> model = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				User account = getThisAccount();
				Collection<Team> teams = account.getTeams();
				Set<User> users = Sets.newHashSet();
				
				for (Team each : teams) {
					for (Membership membership : each.getMemberships()) {
						users.add(membership.getUser());
					}
				}
				
				List<User> result = Lists.newArrayList(users);
				Collections.sort(result);
				return result;
			}
			
		};
		
		ListView<User> membersView = new ListView<User>("member", model) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User user = item.getModelObject();
				IModel<User> model = new UserModel(user);
				item.add(new AvatarImage("avatar", model));
				AbstractLink link = PageSpec.newUserHomeLink("namelink", user);
				link.add(new Label("name", new PropertyModel<String>(model, "name")));
				item.add(link);
				item.add(new Label("displayname", new PropertyModel<String>(model, "displayName")));
			}
			
		};
		
		add(membersView);
	}
	
	private User getThisAccount() {
		return (User) getDefaultModelObject();
	}
}
