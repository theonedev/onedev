package com.pmease.gitplex.web.page.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownMode;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.behavior.menu.MenuBehavior;
import com.pmease.commons.wicket.behavior.menu.MenuPanel;
import com.pmease.commons.wicket.component.menu.LinkItem;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.AvatarByUser;
import com.pmease.gitplex.web.component.avatar.AvatarMode;
import com.pmease.gitplex.web.component.userlink.UserLink;
import com.pmease.gitplex.web.model.UserModel;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.home.admin.AccountListPage;
import com.pmease.gitplex.web.page.security.LoginPage;
import com.pmease.gitplex.web.page.security.LogoutPage;
import com.pmease.gitplex.web.page.security.RegisterPage;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	public LayoutPage() {
	}
	
	public LayoutPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer mainHead = new WebMarkupContainer("mainHead");
		add(mainHead);
		
		mainHead.add(new BookmarkablePageLink<Void>("home", getApplication().getHomePage()));
		mainHead.add(newContextHead("context"));
		mainHead.add(new BookmarkablePageLink<Void>("administration", AccountListPage.class)
					.setVisible(SecurityUtils.canManageSystem()));

		final User user = getCurrentUser();
		boolean signedIn = user != null;

		mainHead.add(new BookmarkablePageLink<Void>("login", LoginPage.class).setVisible(!signedIn));
		mainHead.add(new BookmarkablePageLink<Void>("register", RegisterPage.class).setVisible(!signedIn));
		mainHead.add(new BookmarkablePageLink<Void>("logout", LogoutPage.class).setVisible(signedIn));
		if (user != null) {
			mainHead.add(new BookmarkablePageLink<Void>("notification", 
					AccountNotificationsPage.class, 
					AccountNotificationsPage.paramsOf(user)) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getCurrentUser().getRequestNotifications().isEmpty());
				}
				
			});
		} else {
			mainHead.add(new WebMarkupContainer("notification").setVisible(false));
		}
		
		if (signedIn) {
			final User prevUser = GitPlex.getInstance(UserManager.class).getPrevious();
			
			if (prevUser != null) {
				Link<Void> prevLink = new Link<Void>("prevUser") {

					@Override
					public void onClick() {
						SecurityUtils.getSubject().releaseRunAs();
						setResponsePage(getPage().getClass(), getPageParameters());
					}
					
				};
				prevLink.add(new AvatarByUser("avatar", new UserModel(prevUser), false));
				mainHead.add(prevLink);

				// Use dropdown panel to mimic tooltip as the bootstrap tooltip has the issue 
				// of disappearing when we adjust margin property when hover over the link
				DropdownPanel tooltip = new DropdownPanel("tooltip", false) {

					@Override
					protected Component newContent(String id) {
						return new Label(id, prevUser.getDisplayName() + " is currently running as " 
								+ user.getDisplayName() + ", click to exit the run-as mode");
					}
					
				};
				mainHead.add(tooltip);
				prevLink.add(new DropdownBehavior(tooltip)
						.mode(new DropdownMode.Hover(100))
						.alignWithComponent(prevLink, 0, 50, 100, 50, 7, true));
			} else {
				WebMarkupContainer prevLink = new WebMarkupContainer("prevUser");
				prevLink.add(new WebMarkupContainer("avatar"));
				prevLink.setVisible(false);
				mainHead.add(prevLink);
				mainHead.add(new WebMarkupContainer("tooltip").setVisible(false));
			}
			mainHead.add(new UserLink("user", new UserModel(user), AvatarMode.AVATAR));
			WebMarkupContainer userMenuTrigger = new WebMarkupContainer("userMenuTrigger");
			MenuPanel userMenu = new MenuPanel("userMenu") {

				@Override
				protected List<MenuItem> getMenuItems() {
					List<MenuItem> menuItems = new ArrayList<>();
					menuItems.add(new LinkItem("Profile") {

						@Override
						public void onClick() {
							setResponsePage(ProfileEditPage.class, ProfileEditPage.paramsOf(user));
						}
						
					});
					menuItems.add(new LinkItem("Logout") {

						@Override
						public void onClick() {
							setResponsePage(LogoutPage.class);
						}
						
					});
					return menuItems;
				}
				
			};
			mainHead.add(userMenu);
			userMenuTrigger.add(new MenuBehavior(userMenu).alignWithTrigger(50, 100, 50, 0, 8, true));
			mainHead.add(userMenuTrigger);
		} else {  
			WebMarkupContainer prevLink = new WebMarkupContainer("prevUser");
			prevLink.add(new WebMarkupContainer("avatar"));
			prevLink.setVisible(false);
			mainHead.add(prevLink);
			mainHead.add(new WebMarkupContainer("user").setVisible(false));
			mainHead.add(new WebMarkupContainer("userMenuTrigger").setVisible(false));
			mainHead.add(new WebMarkupContainer("userMenu").setVisible(false));
		}
	}

	protected Component newContextHead(String componentId) {
		return new WebMarkupContainer(componentId);
	}
	
	protected boolean isLoggedIn() {
		return getCurrentUser() != null;
	}
	
	protected boolean isRemembered() {
		return SecurityUtils.getSubject().isRemembered();
	}
	
	protected boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(LayoutPage.class, "layout.css")));
	}
	
}
