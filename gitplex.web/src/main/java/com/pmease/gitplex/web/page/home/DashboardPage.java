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
import com.pmease.gitplex.web.page.account.AccountPage;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;
import com.pmease.gitplex.web.page.layout.LayoutPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

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
				Link<Void> link = new BookmarkablePageLink<>("accountLink", AccountDepotsPage.class, AccountPage.paramsOf(user)); 
				link.add(new Label("accountName", user.getName()));
				item.add(link);
						
				item.add(new MultilineLabel("fullName", user.getFullName()));
				
				item.add(new Link<Void>("runAs") {

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
