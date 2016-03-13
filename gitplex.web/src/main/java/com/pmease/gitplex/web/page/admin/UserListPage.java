package com.pmease.gitplex.web.page.admin;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.depots.DepotListPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class UserListPage extends AdministrationPage {

	private PageableListView<Account> usersView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer usersContainer; 
	
	@Override
	protected String getPageTitle() {
		return "Dashboard";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new ClearableTextField<String>("searchUsers", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(usersContainer);
				target.add(pagingNavigator);
			}

		});
		
		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageSystem());
			}

			@Override
			public void onClick() {
				setResponsePage(new NewUserPage(new Account()));
			}
			
		});
		
		usersContainer = new WebMarkupContainer("usersContainer");
		usersContainer.setOutputMarkupId(true);
		add(usersContainer);
		
		usersContainer.add(usersView = new PageableListView<Account>("users", new LoadableDetachableModel<List<Account>>() {

			@Override
			protected List<Account> load() {
				AccountManager accountManager = GitPlex.getInstance(AccountManager.class);
				List<Account> users = accountManager.allUsers();
				
				String searchInput = searchField.getInput();
				if (StringUtils.isNotBlank(searchInput)) {
					searchInput = searchInput.trim().toLowerCase();
					for (Iterator<Account> it = users.iterator(); it.hasNext();) {
						Account user = it.next();
						String fullName = user.getFullName();
						if (fullName == null)
							fullName = "";
						else
							fullName = fullName.toLowerCase();
						if (!user.getName().toLowerCase().contains(searchInput) && !fullName.contains(searchInput)) {
							it.remove();
						}
					}
				} else {
					searchInput = null;
				}
				Collections.sort(users, new Comparator<Account>() {

					@Override
					public int compare(Account user1, Account user2) {
						return user1.getName().compareTo(user2.getName());
					}
					
				});
				return users;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<Account> item) {
				Account user = item.getModelObject();

				item.add(new Avatar("avatar", item.getModelObject(), null));
				Link<Void> link = new BookmarkablePageLink<>("userLink", DepotListPage.class, AccountPage.paramsOf(user)); 
				link.add(new Label("userName", user.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("fullName", user.getFullName()));
				
				item.add(new Link<Void>("setting") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						setResponsePage(ProfileEditPage.class, params);
					}

				});
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteAccountModal(target) {
							
							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								setResponsePage(UserListPage.this);
							}
							
							@Override
							protected Account getAccount() {
								return item.getModelObject();
							}
						};
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						Account user = item.getModelObject();
						setVisible(SecurityUtils.canManage(user) && !user.equals(getLoginUser()));
					}

				});
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("usersPageNav", usersView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(usersView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}

}
