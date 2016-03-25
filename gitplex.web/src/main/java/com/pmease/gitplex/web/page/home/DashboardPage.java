package com.pmease.gitplex.web.page.home;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
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
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.Constants;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.layout.LayoutPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

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
		
		TextField<String> searchInput;
		add(searchInput = new ClearableTextField<String>("searchUsers", Model.of("")));
		searchInput.add(new OnTypingDoneBehavior(100) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(usersContainer);
				target.add(pagingNavigator);
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
				
				String searchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<Account> it = users.iterator(); it.hasNext();) {
						Account user = it.next();
						if (!user.getName().toLowerCase().contains(searchFor))
							it.remove();
					}
				} else {
					searchFor = null;
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
			protected void populateItem(final ListItem<Account> item) {
				Account user = item.getModelObject();

				item.add(new Avatar("avatar", item.getModelObject(), null));
				Link<Void> link = new BookmarkablePageLink<>("link", AccountOverviewPage.class, AccountPage.paramsOf(user)); 
				link.add(new Label("name", user.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("fullName", user.getFullName()));
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("usersPageNav", usersView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(usersView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DashboardPage.class, "dashboard.css")));
	}

	@Override
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Dashboard");
	}

}
