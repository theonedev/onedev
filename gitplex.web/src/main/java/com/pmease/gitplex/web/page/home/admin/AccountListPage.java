package com.pmease.gitplex.web.page.home.admin;

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

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.repositories.AccountReposPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class AccountListPage extends AdministrationPage {

	private PageableListView<User> accountsView;
	
	private BootstrapPagingNavigator pagingNavigator;
	
	private WebMarkupContainer accountsContainer; 
	
	private TextField<String> searchInput;
	
	private String searchFor;
	
	@Override
	protected String getPageTitle() {
		return "Dashboard";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchInput = new ClearableTextField<String>("searchAccounts", Model.of("")));
		searchInput.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(accountsContainer);
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
				setResponsePage(new NewAccountPage(new User()));
			}
			
		});
		
		accountsContainer = new WebMarkupContainer("accountsContainer");
		accountsContainer.setOutputMarkupId(true);
		add(accountsContainer);
		
		accountsContainer.add(accountsView = new PageableListView<User>("accounts", new LoadableDetachableModel<List<User>>() {

			@Override
			protected List<User> load() {
				Dao dao = GitPlex.getInstance(Dao.class);
				List<User> users = dao.allOf(User.class);
				
				searchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<User> it = users.iterator(); it.hasNext();) {
						User user = it.next();
						if (!user.getName().toLowerCase().contains(searchFor))
							it.remove();
					}
				} else {
					searchFor = null;
				}
				Collections.sort(users, new Comparator<User>() {

					@Override
					public int compare(User user1, User user2) {
						return user1.getName().compareTo(user2.getName());
					}
					
				});
				return users;
			}
			
		}, Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<User> item) {
				User user = item.getModelObject();

				item.add(new Avatar("avatar", item.getModelObject(), null));
				Link<Void> link = new BookmarkablePageLink<>("accountLink", AccountReposPage.class, AccountPage.paramsOf(user)); 
				link.add(new Label("accountName", user.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("fullName", user.getFullName()));
				
				item.add(new Link<Void>("setting") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						setResponsePage(ProfileEditPage.class, params);
					}

				});
				
				item.add(new Link<Void>("runAs") {

					@Override
					public void onClick() {
						User account = item.getModelObject();
						SecurityUtils.getSubject().runAs(account.getPrincipals());
						setResponsePage(AccountReposPage.class, AccountReposPage.paramsOf(account));
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						UserManager userManager = GitPlex.getInstance(UserManager.class);
						User account = item.getModelObject();
						User currentUser = userManager.getCurrent();
						setVisible(!account.equals(currentUser));
					}
					
				});
				
				final Long accountId = user.getId();
				item.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ConfirmDeleteAccountModal(target) {
							
							@Override
							protected void onDeleted(AjaxRequestTarget target) {
								setResponsePage(getPage());
							}
							
							@Override
							protected User getAccount() {
								return GitPlex.getInstance(Dao.class).load(User.class, accountId);
							}
						};
					}

				});
			}
			
		});

		add(pagingNavigator = new BootstrapPagingNavigator("accountsPageNav", accountsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}

}
