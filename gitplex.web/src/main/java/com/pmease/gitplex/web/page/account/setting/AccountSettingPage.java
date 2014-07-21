package com.pmease.gitplex.web.page.account.setting;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.component.user.AvatarByUser;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.setting.api.AccountSettingTab;
import com.pmease.gitplex.web.page.account.setting.members.AccountMembersSettingPage;
import com.pmease.gitplex.web.page.account.setting.profile.AccountProfilePage;
import com.pmease.gitplex.web.page.account.setting.repo.AccountRepositoriesPage;
import com.pmease.gitplex.web.page.account.setting.teams.AccountTeamsPage;
import com.pmease.gitplex.web.page.account.setting.teams.EditTeamPage;

@SuppressWarnings("serial")
public abstract class AccountSettingPage extends AccountPage {

	public AccountSettingPage(PageParameters params) {
		super(params);
	}
	
	@SuppressWarnings("unchecked")
	private List<AccountSettingTab> getAllTabs() {
		List<AccountSettingTab> tabs = Lists.newArrayList();
		tabs.add(new AccountSettingTab(Model.of("Profile"), AccountProfilePage.class));
		tabs.add(new AccountSettingTab(Model.of("Repositories"), AccountRepositoriesPage.class));
		tabs.add(new AccountSettingTab(Model.of("Teams"), new Class[] { AccountTeamsPage.class, EditTeamPage.class }));
		tabs.add(new AccountSettingTab(Model.of("Members"), AccountMembersSettingPage.class));
		
		return tabs;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new UserLink("userlink", new UserModel(getAccount())));
		
		add(new ListView<AccountSettingTab>("setting", getAllTabs()) {

			@Override
			protected void populateItem(ListItem<AccountSettingTab> item) {
				final AccountSettingTab tab = item.getModelObject();
				item.add(tab.newTabLink("link", paramsOf(getAccount())));
				
				item.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return tab.isSelected(getPage()) ? "active" : "";
					}
				}));
			}
			
		});
		
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

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getSubject().isPermitted(ObjectPermission.ofUserAdmin(getAccount()));
	}
}
