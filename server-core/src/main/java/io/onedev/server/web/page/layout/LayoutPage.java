package io.onedev.server.web.page.layout;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.Plugin;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.generalsecuritysetting.GeneralSecuritySettingPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.defaultboard.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.fieldspec.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.issuetemplate.IssueTemplateListPage;
import io.onedev.server.web.page.admin.issuesetting.statespec.IssueStateListPage;
import io.onedev.server.web.page.admin.issuesetting.transitionspec.StateTransitionListPage;
import io.onedev.server.web.page.admin.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.role.NewRolePage;
import io.onedev.server.web.page.admin.role.RoleDetailPage;
import io.onedev.server.web.page.admin.role.RoleListPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.ssh.SshSettingPage;
import io.onedev.server.web.page.admin.sso.SsoConnectorListPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.page.my.accesstoken.MyAccessTokenPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.page.simple.security.LogoutPage;
import io.onedev.server.web.util.WicketUtils;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {
    
	public LayoutPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer sidebar = new WebMarkupContainer("sidebar");
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie("sidebar.minimized");
		if (cookie != null && "true".equals(cookie.getValue())) 
			sidebar.add(AttributeAppender.append("class", "sidebar-minimized"));
		add(sidebar);
		
		MainMenuCustomization customization = OneDev.getInstance(MainMenuCustomization.class);
		
		sidebar.add(new BookmarkablePageLink<Void>("brandLink", customization.getHomePage()));

		sidebar.add(new ListView<SidebarMenu>("menus", new LoadableDetachableModel<List<SidebarMenu>>() {

			@Override
			protected List<SidebarMenu> load() {
				List<SidebarMenu> menus = new ArrayList<>();
				List<SidebarMenuItem> menuItems = new ArrayList<>(customization.getMainMenuItems());
				if (SecurityUtils.isAdministrator()) {
					List<SidebarMenuItem> administrationMenuItems = new ArrayList<>();
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "System Setting", 
							SystemSettingPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "User Management", UserListPage.class, 
							new PageParameters(), Lists.newArrayList(NewUserPage.class, UserPage.class)));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Role Management", RoleListPage.class, 
							new PageParameters(), Lists.newArrayList(NewRolePage.class, RoleDetailPage.class)));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Group Management", GroupListPage.class, 
							new PageParameters(), Lists.newArrayList(NewGroupPage.class, GroupPage.class)));
					
					List<SidebarMenuItem> securitySettingMenuItems = new ArrayList<>();
					securitySettingMenuItems.add(new SidebarMenuItem.Page(null, "General Setting", 
							GeneralSecuritySettingPage.class, new PageParameters()));
					securitySettingMenuItems.add(new SidebarMenuItem.Page(null, "External Authentication", 
							AuthenticatorPage.class, new PageParameters()));
					securitySettingMenuItems.add(new SidebarMenuItem.Page(null, "SSO Providers", 
							SsoConnectorListPage.class, new PageParameters()));
					securitySettingMenuItems.add(new SidebarMenuItem.Page(null, "SSH Setting", 
							SshSettingPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Security Setting", securitySettingMenuItems));
					
					List<SidebarMenuItem> issueSettingMenuItems = new ArrayList<>();
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Custom Fields", 
							IssueFieldListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "States", 
							IssueStateListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "State Transitions", 
							StateTransitionListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Default Boards", 
							DefaultBoardListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Description Templates", 
							IssueTemplateListPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Issue Setting", issueSettingMenuItems));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Mail Setting", 
							MailSettingPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Job Executors", 
							JobExecutorsPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Groovy Scripts", 
							GroovyScriptListPage.class, new PageParameters()));
					
					List<SidebarMenuItem> maintenanceMenuItems = new ArrayList<>();
					maintenanceMenuItems.add(new SidebarMenuItem.Page(null, "Database Backup", 
							DatabaseBackupPage.class, new PageParameters()));
					maintenanceMenuItems.add(new SidebarMenuItem.Page(null, "Server Log", 
							ServerLogPage.class, new PageParameters()));
					maintenanceMenuItems.add(new SidebarMenuItem.Page(null, "Server Information", 
							ServerInformationPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "System Maintenance", maintenanceMenuItems));
					
					menuItems.add(new SidebarMenuItem.SubMenu("gear", "Administration", administrationMenuItems));
				}		
				menus.add(new SidebarMenu(null, menuItems));
				menus.addAll(getSidebarMenus());
				return menus;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<SidebarMenu> item) {
				SidebarMenu menu = item.getModelObject();
				
				SidebarMenu.Header header = menu.getMenuHeader();
				if (header != null) {
					Fragment fragment = new Fragment("header", "menuHeaderFrag", LayoutPage.this);
					fragment.add(new ExternalImage("icon", Model.of(header.getImageUrl())));
					fragment.add(new Label("label", header.getLabel()));
					fragment.add(new DropdownLink("moreInfo") {

						@Override
						protected Component newContent(String id, FloatingPanel dropdown) {
							return header.newMoreInfo(id, dropdown);
						}
						
					});
					item.add(fragment);
				} else {
					item.add(new WebMarkupContainer("header").setVisible(false));
				}
				
				class MenuBody extends Fragment {

					private final List<SidebarMenuItem> menuItems;
					
					public MenuBody(String componentId, List<SidebarMenuItem> menuItems) {
						super(componentId, "menuBodyFrag", LayoutPage.this);
						this.menuItems = menuItems;
					}

					@Override
					protected void onInitialize() {
						super.onInitialize();
						
						add(new ListView<SidebarMenuItem>("items", menuItems) {

							@Override
							protected void populateItem(ListItem<SidebarMenuItem> item) {
								SidebarMenuItem menuItem = item.getModelObject();
								WebMarkupContainer menuLink;
								if (menuItem instanceof SidebarMenuItem.Page) {
									SidebarMenuItem.Page page = (SidebarMenuItem.Page) menuItem;
									menuLink = new BookmarkablePageLink<Void>("link", page.getPageClass(), page.getPageParams());
									menuLink.add(new WebMarkupContainer("arrow").setVisible(false));
									item.add(new WebMarkupContainer("subMenu").setVisible(false));
								} else {
									SidebarMenuItem.SubMenu subMenu = (SidebarMenuItem.SubMenu) menuItem;
									menuLink = new WebMarkupContainer("link");
									menuLink.add(AttributeAppender.append("class", "menu-toggle"));
									menuLink.add(new SpriteImage("arrow", "arrow"));
									MenuBody menuBody = new MenuBody("subMenu", subMenu.getMenuItems()); 
									if (!subMenu.isActive())
										menuBody.add(AttributeAppender.append("style", "display:none;"));
									item.add(menuBody);
								}
								
								int nestLevel = WicketUtils.findParents(item, MenuBody.class).size();
								
								if (menuItem.getIconHref() != null) {
									menuLink.add(new SpriteImage("icon", menuItem.getIconHref()));
									menuLink.add(new WebMarkupContainer("bullet").setVisible(false));
								} else {
									menuLink.add(new WebMarkupContainer("icon").setVisible(false));
									String bulletType = nestLevel % 2 == 0? "menu-bullet-line": "menu-bullet-dot dot";
									menuLink.add(new WebMarkupContainer("bullet").add(AttributeAppender.append("class", bulletType)));
								}
								menuLink.add(AttributeAppender.append("style", "padding-left: " + (25 + (15*(nestLevel-1))) + "px;"));
								menuLink.add(AttributeAppender.append("title", menuItem.getLabel()));
								menuLink.add(new Label("label", menuItem.getLabel()));
								if (menuItem.isActive())
									menuLink.add(AttributeAppender.append("class", "active open"));
								item.add(menuLink);
							}
							
						});
					}
					
				}
				
				item.add(new MenuBody("body", menu.getMenuItems()));
			}
			
		});
		
		Plugin product = AppLoader.getProduct();
		sidebar.add(new Label("productVersion", "Ver. " + product.getVersion()));
		sidebar.add(new ExternalLink("docLink", OneDev.getInstance().getDocRoot() + "/"));
		
		WebMarkupContainer topbar = new WebMarkupContainer("topbar");
		add(topbar);
		
		topbar.add(newTopbarTitle("title"));

		User loginUser = getLoginUser();
		
		topbar.add(new Link<Void>("signIn") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		}.setVisible(loginUser == null));
		
		topbar.add(new BookmarkablePageLink<Void>("brandLink", customization.getHomePage()));
		
		WebMarkupContainer userInfo = new WebMarkupContainer("userInfo");
		if (loginUser != null) {
			userInfo.add(new UserAvatar("avatar", loginUser));
			userInfo.add(new Label("name", loginUser.getDisplayName()));
		} else {
			userInfo.add(new WebMarkupContainer("avatar"));
			userInfo.add(new WebMarkupContainer("name"));
		}
		
		WebMarkupContainer item;
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myProfile", MyProfilePage.class));
		if (getPage() instanceof MyProfilePage)
			item.add(AttributeAppender.append("class", "active"));
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myAvatar", MyAvatarPage.class));
		if (getPage() instanceof MyAvatarPage)
			item.add(AttributeAppender.append("class", "active"));
				
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myPassword", MyPasswordPage.class));
		if (getPage() instanceof MyPasswordPage)
			item.add(AttributeAppender.append("class", "active"));

		userInfo.add(item = new ViewStateAwarePageLink<Void>("mySshKeys", MySshKeysPage.class));
		if (getPage() instanceof MySshKeysPage)
		    item.add(AttributeAppender.append("class", "active"));
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myAccessToken", MyAccessTokenPage.class));
		if (getPage() instanceof MyAccessTokenPage)
		    item.add(AttributeAppender.append("class", "active"));
		
		PrincipalCollection prevPrincipals = SecurityUtils.getSubject().getPreviousPrincipals();
		if (prevPrincipals != null && !prevPrincipals.getPrimaryPrincipal().equals(0L)) {
			Link<Void> signOutLink = new Link<Void>("signOut") {

				@Override
				public void onClick() {
					SecurityUtils.getSubject().releaseRunAs();
					Session.get().warn("Exited impersonation");
					setResponsePage(ProjectListPage.class);
				}
				
			}; 
			signOutLink.add(new Label("label", "Exit Impersonation"));
			userInfo.add(signOutLink);
		} else {
			ViewStateAwarePageLink<Void> signOutLink = new ViewStateAwarePageLink<Void>("signOut", LogoutPage.class); 
			signOutLink.add(new Label("label", "Sign Out"));
			userInfo.add(signOutLink);
		}

		userInfo.setVisible(loginUser != null);
		
		if (getPage() instanceof MyPage)
			userInfo.add(AttributeAppender.append("class", "active"));
		
		topbar.add(userInfo);
	}

	@Override
	protected boolean isPermitted() {
		return getLoginUser() != null || OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new LayoutResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.layout.onDomReady();"));
		response.render(OnLoadHeaderItem.forScript("onedev.server.layout.onLoad();"));
	}
	
	protected List<SidebarMenu> getSidebarMenus() {
		return Lists.newArrayList();
	}

	protected abstract Component newTopbarTitle(String componentId);
	
}
