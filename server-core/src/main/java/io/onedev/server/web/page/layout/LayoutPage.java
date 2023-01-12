package io.onedev.server.web.page.layout;

import com.google.common.collect.Lists;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.Plugin;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.brandlogo.BrandLogoPanel;
import io.onedev.server.web.component.commandpalette.CommandPalettePanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.brandingsetting.BrandingSettingPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentDetailPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;
import io.onedev.server.web.page.admin.buildsetting.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.gpgsigningkey.GpgSigningKeyPage;
import io.onedev.server.web.page.admin.gpgtrustedkeys.GpgTrustedKeysPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupListPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupPage;
import io.onedev.server.web.page.admin.groupmanagement.create.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.defaultboard.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.fieldspec.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.issuetemplate.IssueTemplateListPage;
import io.onedev.server.web.page.admin.issuesetting.linkspec.LinkSpecListPage;
import io.onedev.server.web.page.admin.issuesetting.statespec.IssueStateListPage;
import io.onedev.server.web.page.admin.issuesetting.transitionspec.StateTransitionListPage;
import io.onedev.server.web.page.admin.labelmanagement.LabelManagementPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.notificationtemplatesetting.IssueNotificationTemplatePage;
import io.onedev.server.web.page.admin.notificationtemplatesetting.PullRequestNotificationTemplatePage;
import io.onedev.server.web.page.admin.performancesetting.PerformanceSettingPage;
import io.onedev.server.web.page.admin.pluginsettings.ContributedAdministrationSettingPage;
import io.onedev.server.web.page.admin.rolemanagement.NewRolePage;
import io.onedev.server.web.page.admin.rolemanagement.RoleDetailPage;
import io.onedev.server.web.page.admin.rolemanagement.RoleListPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.servicedesk.ServiceDeskSettingPage;
import io.onedev.server.web.page.admin.sshserverkey.SshServerKeyPage;
import io.onedev.server.web.page.admin.ssosetting.SsoConnectorListPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.usermanagement.*;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.help.IncompatibilitiesPage;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.page.my.accesstoken.MyAccessTokenPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.emailaddresses.MyEmailAddressesPage;
import io.onedev.server.web.page.my.gpgkeys.MyGpgKeysPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;
import io.onedev.server.web.page.my.twofactorauthentication.MyTwoFactorAuthenticationPage;
import io.onedev.server.web.page.simple.security.LoginPage;
import io.onedev.server.web.page.simple.security.LogoutPage;
import io.onedev.server.web.util.WicketUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

import javax.servlet.http.Cookie;
import java.util.*;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {
    
	private AbstractDefaultAjaxBehavior commandPaletteBehavior;
	
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
		
		sidebar.add(new BookmarkablePageLink<Void>("brandLink", HomePage.class) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new BrandLogoPanel("brandLogo"));
				add(new Label("brandName", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						return OneDev.getInstance(SettingManager.class).getBrandingSetting().getName();
					}
					
				}));
			}
			
		});

		sidebar.add(new ListView<SidebarMenu>("menus", new LoadableDetachableModel<List<SidebarMenu>>() {

			@Override
			protected List<SidebarMenu> load() {
				List<SidebarMenu> menus = new ArrayList<>();
				List<SidebarMenuItem> menuItems = new ArrayList<>(customization.getMainMenuItems());
				if (SecurityUtils.isAdministrator()) {
					List<SidebarMenuItem> administrationMenuItems = new ArrayList<>();
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "System Settings", 
							SystemSettingPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Security Settings", 
							SecuritySettingPage.class, new PageParameters()));
					List<SidebarMenuItem> userManagementMenuItems = new ArrayList<>();
					userManagementMenuItems.add(new SidebarMenuItem.Page(null, "Users", UserListPage.class, 
							new PageParameters(), Lists.newArrayList(NewUserPage.class, UserPage.class)));
					userManagementMenuItems.add(new SidebarMenuItem.Page(null, "Invitations", InvitationListPage.class, 
							new PageParameters(), Lists.newArrayList(NewInvitationPage.class)));
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "User Management", userManagementMenuItems));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Role Management", RoleListPage.class, 
							new PageParameters(), Lists.newArrayList(NewRolePage.class, RoleDetailPage.class)));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Group Management", GroupListPage.class, 
							new PageParameters(), Lists.newArrayList(NewGroupPage.class, GroupPage.class)));
					
					List<SidebarMenuItem> authenticationMenuItems = new ArrayList<>();
					authenticationMenuItems.add(new SidebarMenuItem.Page(null, "External Authentication", 
							AuthenticatorPage.class, new PageParameters()));
					authenticationMenuItems.add(new SidebarMenuItem.Page(null, "Single Sign On", 
							SsoConnectorListPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Authentication Source", authenticationMenuItems));
					
					List<SidebarMenuItem> keyManagementMenuItems = new ArrayList<>();
					keyManagementMenuItems.add(new SidebarMenuItem.Page(null, "SSH Server Key", 
							SshServerKeyPage.class, new PageParameters()));
					keyManagementMenuItems.add(new SidebarMenuItem.Page(null, "GPG Signing Key", 
							GpgSigningKeyPage.class, new PageParameters()));
					keyManagementMenuItems.add(new SidebarMenuItem.Page(null, "GPG Trusted Keys", 
							GpgTrustedKeysPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "SSH & GPG Keys", keyManagementMenuItems));
					
					List<SidebarMenuItem> issueSettingMenuItems = new ArrayList<>();
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Custom Fields", 
							IssueFieldListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "States", 
							IssueStateListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "State Transitions", 
							StateTransitionListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Default Boards", 
							DefaultBoardListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Links", 
							LinkSpecListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Description Templates", 
							IssueTemplateListPage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Issue Settings", issueSettingMenuItems));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Job Executors", 
							JobExecutorsPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Agents", 
							AgentListPage.class, AgentListPage.paramsOf(0), Lists.newArrayList(AgentDetailPage.class)));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Mail Settings", 
							MailSettingPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Service Desk Settings", 
							ServiceDeskSettingPage.class, new PageParameters()));
					
					List<SidebarMenuItem> notificationTemplateSettingMenuItems = new ArrayList<>();
					notificationTemplateSettingMenuItems.add(new SidebarMenuItem.Page(null, "Issue", 
							IssueNotificationTemplatePage.class, new PageParameters()));
					notificationTemplateSettingMenuItems.add(new SidebarMenuItem.Page(null, "Pull Request", 
							PullRequestNotificationTemplatePage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Notification Templates", 
							notificationTemplateSettingMenuItems));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Label Management", 
							LabelManagementPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Performance Settings", 
							PerformanceSettingPage.class, new PageParameters()));
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Groovy Scripts", 
							GroovyScriptListPage.class, new PageParameters()));

					List<Class<? extends ContributedAdministrationSetting>> contributedSettingClasses = new ArrayList<>();
					for (AdministrationSettingContribution contribution:OneDev.getExtensions(AdministrationSettingContribution.class)) {
						for (Class<? extends ContributedAdministrationSetting> settingClass: contribution.getSettingClasses())
							contributedSettingClasses.add(settingClass);
					}
					contributedSettingClasses.sort(Comparator.comparingInt(EditableUtils::getOrder));

					Map<String, List<SidebarMenuItem>> contributedMenuItems = new HashMap<>();
					for (var contributedSettingClass: contributedSettingClasses) {
						var group = EditableUtils.getGroup(contributedSettingClass);
						if (group == null)
							group = "";
						var contributedMenuItemsOfGroup = contributedMenuItems.get(group);
						if (contributedMenuItemsOfGroup == null) {
							contributedMenuItemsOfGroup = new ArrayList<>();
							contributedMenuItems.put(group, contributedMenuItemsOfGroup);
						}
						contributedMenuItemsOfGroup.add(new SidebarMenuItem.Page(
								null,
								EditableUtils.getDisplayName(contributedSettingClass),
								ContributedAdministrationSettingPage.class,
								ContributedAdministrationSettingPage.paramsOf(contributedSettingClass)));
					}
					for (var entry: contributedMenuItems.entrySet()) {
						if (entry.getKey().length() == 0) {
							administrationMenuItems.addAll(entry.getValue());
						} else {
							administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, entry.getKey(), entry.getValue()));
						}
					}
					
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Branding", 
							BrandingSettingPage.class, new PageParameters()));
					
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
									menuLink = new ViewStateAwarePageLink<Void>("link", page.getPageClass(), page.getPageParams());
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
		sidebar.add(new ExternalLink("productVersion", "https://code.onedev.io/onedev/server")
				.setBody(Model.of("OneDev " + product.getVersion())));
		sidebar.add(new BookmarkablePageLink<Void>("incompatibilities", IncompatibilitiesPage.class));
		
		WebMarkupContainer topbar = new WebMarkupContainer("topbar");
		add(topbar);
		
		topbar.add(newTopbarTitle("title"));

		topbar.add(new ModalLink("showCommandPalette") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CommandPalettePanel(id) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
					
				};
			}

		});
		topbar.add(new Link<Void>("darkMode") {

			@Override
			public void onClick() {
				toggleDarkMode();
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new SpriteImage("icon", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (isDarkMode())
							return "moon";
						else
							return "sun";
					}
					
				}));
			}
			
		});
		
		
		User loginUser = getLoginUser();
		
		topbar.add(new Link<Void>("signIn") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		}.setVisible(loginUser == null));
		
		topbar.add(new BookmarkablePageLink<Void>("brandLink", HomePage.class) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new BrandLogoPanel("brandLogo"));
			}
			
		});
		
		WebMarkupContainer userInfo = new WebMarkupContainer("userInfo");
		if (loginUser != null) {
			userInfo.add(new UserAvatar("avatar", loginUser));
			userInfo.add(new Label("name", loginUser.getDisplayName()));
			if (loginUser.getEmailAddresses().isEmpty()) {
				userInfo.add(new WebMarkupContainer("warningIcon"));
				userInfo.add(new WebMarkupContainer("hasUnverifiedLink").setVisible(false));
				userInfo.add(new ViewStateAwarePageLink<Void>("noPrimaryAddressLink", MyEmailAddressesPage.class));
			} else if (loginUser.getEmailAddresses().stream().anyMatch(it->!it.isVerified())) {
				userInfo.add(new WebMarkupContainer("warningIcon"));
				userInfo.add(new ViewStateAwarePageLink<Void>("hasUnverifiedLink", MyEmailAddressesPage.class));
				userInfo.add(new WebMarkupContainer("noPrimaryAddressLink").setVisible(false));
			} else {
				userInfo.add(new WebMarkupContainer("warningIcon").setVisible(false));
				userInfo.add(new WebMarkupContainer("hasUnverifiedLink").setVisible(false));
				userInfo.add(new WebMarkupContainer("noPrimaryAddressLink").setVisible(false));
			}
		} else {
			userInfo.add(new WebMarkupContainer("avatar"));
			userInfo.add(new WebMarkupContainer("name"));
			userInfo.add(new WebMarkupContainer("warningIcon"));
			userInfo.add(new WebMarkupContainer("warningLink"));
		}
		
		WebMarkupContainer item;
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myProfile", MyProfilePage.class));
		if (getPage() instanceof MyProfilePage)
			item.add(AttributeAppender.append("class", "active"));
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myEmailSetting", MyEmailAddressesPage.class));
		if (getPage() instanceof MyEmailAddressesPage)
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
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myGpgKeys", MyGpgKeysPage.class));
		if (getPage() instanceof MyGpgKeysPage)
		    item.add(AttributeAppender.append("class", "active"));
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myAccessToken", MyAccessTokenPage.class));
		if (getPage() instanceof MyAccessTokenPage)
		    item.add(AttributeAppender.append("class", "active"));
		
		userInfo.add(item = new ViewStateAwarePageLink<Void>("myTwoFactorAuthentication", MyTwoFactorAuthenticationPage.class));
		if (getPage() instanceof MyTwoFactorAuthenticationPage)
		    item.add(AttributeAppender.append("class", "active"));
		
		PrincipalCollection prevPrincipals = SecurityUtils.getSubject().getPreviousPrincipals();
		if (prevPrincipals != null && !prevPrincipals.getPrimaryPrincipal().equals(0L)) {
			Link<Void> signOutLink = new Link<Void>("signOut") {

				@Override
				public void onClick() {
					SecurityUtils.getSubject().releaseRunAs();
					Session.get().warn("Exited impersonation");
					throw new RestartResponseException(HomePage.class);
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
		
		add(commandPaletteBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				new ModalPanel(target) {
					
					@Override
					protected Component newContent(String id) {
						return new CommandPalettePanel(id) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								close();
							}
							
						};
					}
					
				};
			}
			
		});
	}

	@Override
	protected boolean isPermitted() {
		return getLoginUser() != null || OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new LayoutResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript(String.format("onedev.server.layout.onDomReady(%s);", 
				commandPaletteBehavior.getCallbackFunction())));
		response.render(OnLoadHeaderItem.forScript("onedev.server.layout.onLoad();"));
	}
	
	protected List<SidebarMenu> getSidebarMenus() {
		return Lists.newArrayList();
	}

	protected abstract Component newTopbarTitle(String componentId);
	
}
