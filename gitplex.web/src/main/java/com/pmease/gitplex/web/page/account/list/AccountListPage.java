package com.pmease.gitplex.web.page.account.list;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.navigator.PagingNavigator;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.setting.AvatarEditPage;
import com.pmease.gitplex.web.page.account.setting.PasswordEditPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.main.MainPage;

@SuppressWarnings("serial")
public class AccountListPage extends MainPage {

	private PageableListView<User> accountsView;
	
	private PagingNavigator pagingNavigator;
	
	private WebMarkupContainer accountsContainer; 
	
	private TextField<String> searchInput;
	
	private String searchFor;
	
	@Override
	protected String getPageTitle() {
		return "Accounts";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchInput = new ClearableTextField<String>("searchAccounts", Model.of("")));
		searchInput.add(new OnSearchingBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// IE triggers "input" event when the focused on the search input even if nothing is 
				// input into search box yet. To work around this issue, we compare search string 
				// against previous value to only update the branches table if there is an actual 
				// change.
				String newSearchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(newSearchFor))
					newSearchFor = newSearchFor.trim().toLowerCase();
				else
					newSearchFor = null;
				if (!ObjectUtils.equals(newSearchFor, searchFor))
					super.onUpdate(target);
			}
			
		});
		
		add(new Link<Void>("addNew") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.getSubject().isPermitted(ObjectPermission.ofSystemAdmin()));
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

				item.add(new AvatarByUser("avatar", item.getModel(), false));
				Link<Void> link = new BookmarkablePageLink<>("accountLink", AccountOverviewPage.class, AccountPage.paramsOf(user)); 
				link.add(new Label("accountName", user.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("fullName", user.getFullName()));
				
				item.add(new Link<Void>("editProfile") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						addPrevPageParam(params);
						setResponsePage(ProfileEditPage.class, params);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}
					
				});
				
				item.add(new Link<Void>("changePassword") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						addPrevPageParam(params);
						setResponsePage(PasswordEditPage.class, params);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						User account = item.getModelObject();
						setVisible(account.getPassword() != null 
								&& SecurityUtils.canManage(account));
					}
					
				});
				
				item.add(new Link<Void>("changeAvatar") {

					@Override
					public void onClick() {
						PageParameters params = AccountPage.paramsOf(item.getModelObject());
						addPrevPageParam(params);
						setResponsePage(AvatarEditPage.class, params);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.canManage(item.getModelObject()));
					}
					
				});
				
				item.add(new Link<Void>("becomeAccount") {

					@Override
					public void onClick() {
						User account = item.getModelObject();
						SecurityUtils.getSubject().runAs(account.getPrincipals());
						setResponsePage(getPage().getClass(), getPageParameters());
					}
					
					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						UserManager userManager = GitPlex.getInstance(UserManager.class);
						User account = item.getModelObject();
						User currentUser = userManager.getCurrent();
						setVisible(!account.equals(currentUser) && SecurityUtils.canManage(account));
					}
					
				});
				
				final Long accountId = user.getId();
				item.add(new Link<Void>("delete") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new ConfirmBehavior("Do you really want to delete this account?"));				
					}

					@Override
					public void onClick() {
						Dao dao = GitPlex.getInstance(Dao.class);
						GitPlex.getInstance(UserManager.class).delete(dao.load(User.class, accountId));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						User account = item.getModelObject();
						User currentUser = getCurrentUser();
						setVisible(!account.isRoot() 
								&& !account.equals(currentUser) 
								&& SecurityUtils.canManage(account));
					}
					
				});
			}
			
		});

		add(pagingNavigator = new PagingNavigator("accountsPageNav", accountsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accountsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(AccountListPage.class, "account-list.css")));
	}

	private class OnSearchingBehavior extends OnTypingDoneBehavior {

		public OnSearchingBehavior() {
			super(100);
		}

		@Override
		protected void onUpdate(AjaxRequestTarget target) {
			target.add(accountsContainer);
			target.add(pagingNavigator);
		}

	}
}
