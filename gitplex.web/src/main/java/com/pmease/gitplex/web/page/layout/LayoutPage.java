package com.pmease.gitplex.web.page.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.accountselector.OrganizationSelector;
import com.pmease.gitplex.web.component.avatar.AvatarLink;
import com.pmease.gitplex.web.component.depotselector.DepotSelector;
import com.pmease.gitplex.web.page.account.notifications.NotificationListPage;
import com.pmease.gitplex.web.page.account.overview.AccountOverviewPage;
import com.pmease.gitplex.web.page.account.overview.NewOrganizationPage;
import com.pmease.gitplex.web.page.account.setting.ProfileEditPage;
import com.pmease.gitplex.web.page.admin.account.AccountListPage;
import com.pmease.gitplex.web.page.base.BasePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
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
		
		head.add(new DropdownLink("organizations") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null);
			}

			@Override
			protected Component newContent(String id) {
				return new OrganizationSelector(id, new AbstractReadOnlyModel<Account>() {

					@Override
					public Account getObject() {
						return getLoginUser();
					}

				}, Account.idOf(getAccount())) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Account account) {
						LayoutPage.this.onSelect(target, account);
					}

				};
			}
			
		});
		head.add(new DropdownLink("repositories") {

			@Override
			protected Component newContent(String id) {
				return new DepotSelector(id, new LoadableDetachableModel<Collection<Depot>>() {

					@Override
					protected Collection<Depot> load() {
						return GitPlex.getInstance(DepotManager.class).findAllAccessible(null, getLoginUser());
					}
					
				}, Depot.idOf(getDepot())) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Depot depot) {
						LayoutPage.this.onSelect(target, depot);
					}

				};
			}
			
		});
		
		if (isLoggedIn()) {
			head.add(new DropdownLink("createNewDropdown") {

				@Override
				protected Component newContent(String id) {
					Fragment fragment = new Fragment(id, "createNewFrag", LayoutPage.this);
					fragment.add(new BookmarkablePageLink<Void>("createNewDepot", CreateDepotPage.class));
					fragment.add(new BookmarkablePageLink<Void>("createNewOrganization", 
							NewOrganizationPage.class, NewOrganizationPage.paramsOf(getLoginUser())));
					return fragment;
				}
				
			});
		} else {
			head.add(new WebMarkupContainer("createNewDropdown").setVisible(false));
		}
		
		head.add(new BookmarkablePageLink<Void>("administration", AccountListPage.class)
				.setVisible(SecurityUtils.canManageSystem()));
		
		Account user = getLoginUser();
		boolean signedIn = user != null;

		head.add(new Link<Void>("login") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		}.setVisible(!signedIn));
		
		head.add(new BookmarkablePageLink<Void>("register", RegisterPage.class).setVisible(!signedIn));
		head.add(new BookmarkablePageLink<Void>("logout", LogoutPage.class).setVisible(signedIn));
		if (user != null) {
			head.add(new BookmarkablePageLink<Void>("notifications", 
					NotificationListPage.class, 
					NotificationListPage.paramsOf(user)) {
	
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getLoginUser().getRequestNotifications().isEmpty());
				}
				
			});
		} else {
			head.add(new WebMarkupContainer("notifications").setVisible(false));
		}
		
		if (signedIn) {
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
	
	@Nullable
	protected Account getAccount() {
		return getLoginUser();
	}

	@Nullable
	protected Depot getDepot() {
		return null;
	}
	
	protected void onSelect(AjaxRequestTarget target, Account account) {
		setResponsePage(AccountOverviewPage.class, AccountOverviewPage.paramsOf(account));
	}
	
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotFilePage.class, DepotFilePage.paramsOf(depot));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new LayoutResourceReference()));
	}
	
}
