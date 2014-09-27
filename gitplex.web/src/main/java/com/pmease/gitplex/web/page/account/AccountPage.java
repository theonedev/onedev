package com.pmease.gitplex.web.page.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.repository.RepositoriesPage;
import com.pmease.gitplex.web.page.account.team.AccountTeamsPage;
import com.pmease.gitplex.web.page.account.team.EditTeamPage;

@SuppressWarnings("serial")
public abstract class AccountPage extends AccountBasePage {

	public AccountPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new UserLink("userAvatar", new UserModel(getAccount()), AvatarMode.AVATAR));
		add(new UserLink("userName", new UserModel(getAccount()), AvatarMode.NAME));
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new AccountTab(Model.of("Repositories"), "fa fa-repo", RepositoriesPage.class));
		tabs.add(new AccountTab(Model.of("Teams"), "fa fa-group-o", AccountTeamsPage.class, EditTeamPage.class));
		tabs.add(new AccountTab(Model.of("Members"), "fa fa-user-o", MemberSettingPage.class));
		tabs.add(new AccountTab(Model.of("Profile"), "fa fa-gear", AccountProfilePage.class));

		add(new Tabbable("tabs", tabs));
		
		IModel<List<User>> model = new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				UserManager um = GitPlex.getInstance(UserManager.class);
				List<User> users = 
							um.getManagableAccounts(um.getCurrent());
				
				List<User> result = Lists.newArrayList();
				
				for (User each : users) {
					if (!Objects.equal(each, getAccount())) {
						result.add(each);
					}
				}
				
				return result;
			}
			
		};
		
		ListView<User> listView = new ListView<User>("owner", model) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User user = item.getModelObject();
				
				PageParameters params = paramsOf(user);
				AbstractLink link = new BookmarkablePageLink<Void>("link",
						AccountProfilePage.class,
						params);
				link.add(new AvatarByUser("avatar", item.getModel()));
				item.add(link);
				link.add(new Label("name", user.getName()));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(!getList().isEmpty());
			}
		};
		
		add(listView);
	}

}
