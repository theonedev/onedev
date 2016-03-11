package com.pmease.gitplex.web.page.layout;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.dropdown.DropdownHover;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.avatar.Avatar;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.page.account.notifications.AccountNotificationsPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.admin.UserListPage;
import com.pmease.gitplex.web.page.base.BasePage;
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
		
		WebMarkupContainer head = new WebMarkupContainer("mainHead");
		add(head);
		
		head.add(new BookmarkablePageLink<Void>("home", getApplication().getHomePage()));
		head.add(newContextHead("context"));
		head.add(new BookmarkablePageLink<Void>("administration", UserListPage.class)
					.setVisible(SecurityUtils.canManageSystem()));

		Account user = getLoginUser();
		boolean signedIn = user != null;

		head.add(new BookmarkablePageLink<Void>("login", LoginPage.class).setVisible(!signedIn));
		head.add(new BookmarkablePageLink<Void>("register", RegisterPage.class).setVisible(!signedIn));
		head.add(new BookmarkablePageLink<Void>("logout", LogoutPage.class).setVisible(signedIn));
		if (user != null) {
			head.add(new BookmarkablePageLink<Void>("notification", 
					AccountNotificationsPage.class, 
					AccountNotificationsPage.paramsOf(user)) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getLoginUser().getRequestNotifications().isEmpty());
				}
				
			});
		} else {
			head.add(new WebMarkupContainer("notification").setVisible(false));
		}
		
		if (signedIn) {
			final Account prevUser = GitPlex.getInstance(AccountManager.class).getPrevious();
			
			if (prevUser != null) {
				Link<Void> prevLink = new Link<Void>("prevUser") {

					@Override
					public void onClick() {
						SecurityUtils.getSubject().releaseRunAs();
						setResponsePage(getPage().getClass(), getPageParameters());
					}
					
				};
				prevLink.add(new Avatar("avatar", prevUser, null));
				head.add(prevLink);

				// Use dropdown panel to mimic tooltip as the bootstrap tooltip has the issue 
				// of disappearing when we adjust margin property when hover over the link
				
				prevLink.add(new DropdownHover(prevLink, new AlignPlacement(0, 50, 100, 50, 0), 100) {

					@Override
					protected void onInitialize(FloatingPanel dropdown) {
						super.onInitialize(dropdown);
						dropdown.add(AttributeAppender.replace("id", "runas-tooltip"));
					}

					@Override
					protected Component newContent(String id) {
						return new Label(id, prevUser.getDisplayName() + " is currently running as " 
								+ user.getDisplayName() + ", click to exit the run-as mode");
					}
					
				});
			} else {
				WebMarkupContainer prevLink = new WebMarkupContainer("prevUser");
				prevLink.add(new WebMarkupContainer("avatar"));
				prevLink.setVisible(false);
				head.add(prevLink);
				head.add(new WebMarkupContainer("tooltip").setVisible(false));
			}
			head.add(new AvatarLink("user", user, null));
			head.add(new MenuLink("userMenuTrigger", new AlignPlacement(50, 100, 50, 0, 8)) {

				@Override
				protected List<MenuItem> getMenuItems() {
					List<MenuItem> menuItems = new ArrayList<>();
					menuItems.add(new MenuItem() {

						@Override
						public String getIconClass() {
							return null;
						}

						@Override
						public String getLabel() {
							return "Profile";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									setResponsePage(ProfileEditPage.class, ProfileEditPage.paramsOf(user));									
								}
								
							};
						}
						
					});
					menuItems.add(new MenuItem() {

						@Override
						public String getIconClass() {
							return null;
						}

						@Override
						public String getLabel() {
							return "Logout";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									setResponsePage(LogoutPage.class);									
								}
								
							};
						}
						
					});
					return menuItems;
				}
				
			});
		} else {  
			WebMarkupContainer prevLink = new WebMarkupContainer("prevUser");
			prevLink.add(new WebMarkupContainer("avatar"));
			prevLink.setVisible(false);
			head.add(prevLink);
			head.add(new WebMarkupContainer("user").setVisible(false));
			head.add(new WebMarkupContainer("userMenuTrigger").setVisible(false));
			head.add(new WebMarkupContainer("userMenu").setVisible(false));
		}
		
		add(new WebMarkupContainer("mainFoot") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isFootVisible());
			}
			
		});
	}

	protected Component newContextHead(String componentId) {
		return new WebMarkupContainer(componentId);
	}
	
	protected boolean isLoggedIn() {
		return getLoginUser() != null;
	}
	
	protected boolean isRemembered() {
		return SecurityUtils.getSubject().isRemembered();
	}
	
	protected boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	protected boolean isFootVisible() {
		return true;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(LayoutPage.class, "layout.css")));
	}
	
}
