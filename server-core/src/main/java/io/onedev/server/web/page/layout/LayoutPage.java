package io.onedev.server.web.page.layout;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Alert;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.updatecheck.UpdateCheckManager;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.brandlogo.BrandLogoPanel;
import io.onedev.server.web.component.commandpalette.CommandPalettePanel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.user.UserAvatar;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.admin.alertsettings.AlertSettingPage;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.brandingsetting.BrandingSettingPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentDetailPage;
import io.onedev.server.web.page.admin.buildsetting.agent.AgentListPage;
import io.onedev.server.web.page.admin.buildsetting.jobexecutor.JobExecutorsPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.emailtemplates.*;
import io.onedev.server.web.page.admin.gpgsigningkey.GpgSigningKeyPage;
import io.onedev.server.web.page.admin.gpgtrustedkeys.GpgTrustedKeysPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupListPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupPage;
import io.onedev.server.web.page.admin.groupmanagement.create.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.commitmessagefixpatterns.CommitMessageFixPatternsPage;
import io.onedev.server.web.page.admin.issuesetting.defaultboard.DefaultBoardListPage;
import io.onedev.server.web.page.admin.issuesetting.fieldspec.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.integritycheck.CheckIssueIntegrityPage;
import io.onedev.server.web.page.admin.issuesetting.issuetemplate.IssueTemplateListPage;
import io.onedev.server.web.page.admin.issuesetting.linkspec.LinkSpecListPage;
import io.onedev.server.web.page.admin.issuesetting.statespec.IssueStateListPage;
import io.onedev.server.web.page.admin.issuesetting.timetracking.TimeTrackingSettingPage;
import io.onedev.server.web.page.admin.issuesetting.transitionspec.StateTransitionListPage;
import io.onedev.server.web.page.admin.labelmanagement.LabelManagementPage;
import io.onedev.server.web.page.admin.mailservice.MailServicePage;
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
import io.onedev.server.web.page.my.accesstoken.MyAccessTokensPage;
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
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.criterion.Order;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static io.onedev.server.model.Alert.PROP_DATE;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	private AbstractDefaultAjaxBehavior commandPaletteBehavior;

	private AbstractDefaultAjaxBehavior newVersionStatusBehavior;

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
						return getSettingManager().getBrandingSetting().getName();
					}

				}));
			}

		});

		sidebar.add(new ListView<SidebarMenu>("menus", new LoadableDetachableModel<>() {

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
					if (isSubscriptionActive()) {
						issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Time Tracking",
								TimeTrackingSettingPage.class, new PageParameters()));
					}
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Description Templates",
							IssueTemplateListPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Commit Message Fix Patterns",
							CommitMessageFixPatternsPage.class, new PageParameters()));
					issueSettingMenuItems.add(new SidebarMenuItem.Page(null, "Check Workflow Integrity",
							CheckIssueIntegrityPage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Issue Settings", issueSettingMenuItems));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Job Executors",
							JobExecutorsPage.class, new PageParameters()));
					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Agents",
							AgentListPage.class, AgentListPage.paramsOf(0), Lists.newArrayList(AgentDetailPage.class)));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Mail Service",
							MailServicePage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Service Desk Settings",
							ServiceDeskSettingPage.class, new PageParameters()));

					List<SidebarMenuItem> emailTemplatesMenuItems = new ArrayList<>();
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Issue Notification",
							IssueNotificationTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Issue Notification Unsubscribed",
							IssueNotificationUnsubscribedTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Service Desk Issue Opened",
							ServiceDeskIssueOpenedTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Service Desk Issue Open Failed",
							ServiceDeskIssueOpenFailedTemplatePage.class, new PageParameters()));
					if (isSubscriptionActive()) {
						emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Issue Stopwatch Overdue",
								StopwatchOverdueTemplatePage.class, new PageParameters()));
					}
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Pull Request Notification",
							PullRequestNotificationTemplatePage.class, new PageParameters()));

					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Pull Request Notification Unsubscribed",
							PullRequestNotificationUnsubscribedTemplatePage.class, new PageParameters()));

					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "User Invitation",
							UserInvitationTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Email Verification",
							EmailVerificationTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "Password Reset",
							PasswordResetTemplatePage.class, new PageParameters()));
					emailTemplatesMenuItems.add(new SidebarMenuItem.Page(null, "System Alert",
							AlertTemplatePage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "Email Templates",
							emailTemplatesMenuItems));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Alert Settings",
							AlertSettingPage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Label Management",
							LabelManagementPage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Performance Settings",
							PerformanceSettingPage.class, new PageParameters()));

					administrationMenuItems.add(new SidebarMenuItem.Page(null, "Groovy Scripts",
							GroovyScriptListPage.class, new PageParameters()));

					List<Class<? extends ContributedAdministrationSetting>> contributedSettingClasses = new ArrayList<>();
					for (AdministrationSettingContribution contribution : OneDev.getExtensions(AdministrationSettingContribution.class)) {
						for (Class<? extends ContributedAdministrationSetting> settingClass : contribution.getSettingClasses())
							contributedSettingClasses.add(settingClass);
					}
					contributedSettingClasses.sort(Comparator.comparingInt(EditableUtils::getOrder));

					Map<String, List<SidebarMenuItem>> contributedMenuItems = new HashMap<>();
					for (var contributedSettingClass : contributedSettingClasses) {
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
					for (var entry : contributedMenuItems.entrySet()) {
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
					var servers = getClusterManager().getServerAddresses();
					if (servers.size() > 1) {
						List<SidebarMenuItem> serverLogMenuItems = new ArrayList<>();
						List<SidebarMenuItem> serverInformationMenuItems = new ArrayList<>();
						for (var server : servers) {
							serverLogMenuItems.add(new SidebarMenuItem.Page(null, server,
									ServerLogPage.class, ServerLogPage.paramsOf(server)));
							serverInformationMenuItems.add(new SidebarMenuItem.Page(null, server,
									ServerInformationPage.class, ServerInformationPage.paramsOf(server)));
						}
						maintenanceMenuItems.add(new SidebarMenuItem.SubMenu(null, "Server Log", serverLogMenuItems));
						maintenanceMenuItems.add(new SidebarMenuItem.SubMenu(null, "Server Information", serverInformationMenuItems));
					} else {
						maintenanceMenuItems.add(new SidebarMenuItem.Page(null, "Server Log",
								ServerLogPage.class, new PageParameters()));
						maintenanceMenuItems.add(new SidebarMenuItem.Page(null, "Server Information",
								ServerInformationPage.class, new PageParameters()));
					}

					administrationMenuItems.add(new SidebarMenuItem.SubMenu(null, "System Maintenance", maintenanceMenuItems));
					for (var contribution: OneDev.getExtensions(AdministrationMenuContribution.class))
						administrationMenuItems.addAll(contribution.getAdministrationMenuItems());

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

						add(new ListView<>("items", menuItems) {

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
									String bulletType = nestLevel % 2 == 0 ? "menu-bullet-line" : "menu-bullet-dot dot";
									menuLink.add(new WebMarkupContainer("bullet").add(AttributeAppender.append("class", bulletType)));
								}
								menuLink.add(AttributeAppender.append("style", "padding-left: " + (25 + (15 * (nestLevel - 1))) + "px;"));
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

		var version = AppLoader.getProduct().getVersion();
		sidebar.add(new Label("productVersion", "OneDev " + version));
		sidebar.add(new WebMarkupContainer("tryEE") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isSubscriptionActive());
			}
		});

		String commitHash;
		try (var is = new FileInputStream(new File(Bootstrap.installDir, "release.properties"))) {
			var releaseProps = new Properties();
			releaseProps.load(is);
			commitHash = releaseProps.getProperty("commit");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		var checkUpdateUrl = "https://onedev.io/check-update/" + commitHash;
		sidebar.add(new AjaxLink<Void>("checkUpdate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				getUpdateCheckManager().clearCache();
				throw new RedirectToUrlException(checkUpdateUrl);
			}

		});

		sidebar.add(new WebMarkupContainer("tryEEMenuItem") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isSubscriptionActive());
			}
		});
		sidebar.add(new BookmarkablePageLink<Void>("incompatibilities", IncompatibilitiesPage.class));
		sidebar.add(new Label("bugReport", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				if (isSubscriptionActive() && SecurityUtils.isAdministrator())
					return "Bug Report";
				else
					return "Support & Bug Report";
			}

		}));
		if (SecurityUtils.isAdministrator())
			sidebar.add(getSubscriptionManager().renderSupportRequestLink("supportRequest"));
		else
			sidebar.add(new WebMarkupContainer("supportRequest").setVisible(false));

		WebMarkupContainer topbar = new WebMarkupContainer("topbar");
		add(topbar);

		topbar.add(newTopbarTitle("title"));

		DropdownLink alertsLink;
		topbar.add(alertsLink = new DropdownLink("alerts") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment alertsFrag = new Fragment(id, "alertsFrag", LayoutPage.this);
				alertsFrag.add(new AjaxLink<Void>("clear") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						getAlertManager().clear();
						notifyObservableChange(target, Alert.getChangeObservable());
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(SecurityUtils.isAdministrator());
					}
				});

				List<IColumn<Alert, Void>> columns = new ArrayList<>();

				columns.add(new AbstractColumn<>(Model.of("When")) {

					@Override
					public void populateItem(Item<ICellPopulator<Alert>> cellItem, String componentId, IModel<Alert> rowModel) {
						cellItem.add(new Label(componentId, DateUtils.formatDateTime(rowModel.getObject().getDate())));
					}

					@Override
					public String getCssClass() {
						return "text-nowrap";
					}
				});
				columns.add(new AbstractColumn<>(Model.of("Message")) {

					@Override
					public void populateItem(Item<ICellPopulator<Alert>> cellItem, String componentId, IModel<Alert> rowModel) {
						var alert = rowModel.getObject();
						var fragment = new Fragment(componentId, "alertMessageFrag", LayoutPage.this);
						fragment.add(new Label("subject", alert.getSubject()).setEscapeModelStrings(false));

						var detail = new Label("detail", new AbstractReadOnlyModel<String>() {
							@Override
							public String getObject() {
								return rowModel.getObject().getDetail();
							}

						}).setEscapeModelStrings(false).setVisible(false);

						fragment.add(detail);

						fragment.add(new AjaxLink<Void>("showDetail") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								setVisible(false);
								detail.setVisible(true);
								target.add(fragment);
							}

						}.setVisible(alert.getDetail() != null));

						fragment.setOutputMarkupId(true);
						cellItem.add(fragment);
					}
				});

				if (SecurityUtils.isAdministrator()) {
					columns.add(new AbstractColumn<>(Model.of("")) {

						@Override
						public void populateItem(Item<ICellPopulator<Alert>> cellItem, String componentId, IModel<Alert> rowModel) {
							Fragment actionFrag = new Fragment(componentId, "alertActionFrag", LayoutPage.this);
							actionFrag.add(new AjaxLink<Void>("delete") {
								@Override
								public void onClick(AjaxRequestTarget target) {
									getAlertManager().delete(rowModel.getObject());
									notifyObservableChange(target, Alert.getChangeObservable());
								}
							});
							cellItem.add(actionFrag);
						}
					});
				}

				SortableDataProvider<Alert, Void> dataProvider = new SortableDataProvider<Alert, Void>() {

					@Override
					public Iterator<? extends Alert> iterator(long first, long count) {
						var criteria = EntityCriteria.of(Alert.class);
						criteria.addOrder(Order.desc(PROP_DATE));
						return getAlertManager().query(criteria, (int)first, (int)count).iterator();
					}

					@Override
					public long size() {
						return getAlertManager().count();
					}

					@Override
					public IModel<Alert> model(Alert object) {
						Long id = object.getId();
						return new LoadableDetachableModel<Alert>() {

							@Override
							protected Alert load() {
								return getAlertManager().load(id);
							}

						};
					}
				};

				alertsFrag.add(new DefaultDataTable<>("alerts", columns, dataProvider,
						WebConstants.PAGE_SIZE, null));
				alertsFrag.add(new ChangeObserver() {
					@Override
					protected Collection<String> findObservables() {
						return Sets.newHashSet(Alert.getChangeObservable());
					}
				});
				alertsFrag.setOutputMarkupId(true);
				return alertsFrag;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getAlertManager().count() != 0);
			}

		});
		alertsLink.setOutputMarkupPlaceholderTag(true);

		topbar.add(new ChangeObserver() {
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				var count = getAlertManager().count();
				if (count != 0 && !alertsLink.isVisible() || count == 0 && alertsLink.isVisible())
					handler.add(alertsLink);
			}

			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(Alert.getChangeObservable());
			}
		});

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

		topbar.add(new AjaxLink<Void>("newVersionStatus") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new WebMarkupContainer("icon") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						var newVersionStatus = getUpdateCheckManager().getNewVersionStatus();
						if (newVersionStatus != null) {
							tag.put("src", "/~img/new-" + newVersionStatus + ".svg");
							tag.put("onload", "onedev.server.layout.onNewVersionStatusIconLoaded();");
						} else {
							tag.put("src", checkUpdateUrl + "/icon.svg");
							var script = String.format("onedev.server.layout.onNewVersionStatusIconLoaded(%s);",
									newVersionStatusBehavior.getCallbackFunction(explicit("newVersionStatus")));
							tag.put("onload", script);
						}
					}

				});
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				getUpdateCheckManager().clearCache();
				throw new RedirectToUrlException(checkUpdateUrl);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getSettingManager().getSystemSetting().isDisableAutoUpdateCheck());
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
				userInfo.add(new WebMarkupContainer("hasUnverifiedLink").setVisible(false));
				userInfo.add(new ViewStateAwarePageLink<Void>("noPrimaryAddressLink", MyEmailAddressesPage.class));
			} else if (loginUser.getEmailAddresses().stream().anyMatch(it->!it.isVerified())) {
				userInfo.add(new ViewStateAwarePageLink<Void>("hasUnverifiedLink", MyEmailAddressesPage.class));
				userInfo.add(new WebMarkupContainer("noPrimaryAddressLink").setVisible(false));
			} else {
				userInfo.add(new WebMarkupContainer("hasUnverifiedLink").setVisible(false));
				userInfo.add(new WebMarkupContainer("noPrimaryAddressLink").setVisible(false));
			}
		} else {
			userInfo.add(new WebMarkupContainer("avatar"));
			userInfo.add(new WebMarkupContainer("name"));
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

		userInfo.add(item = new ViewStateAwarePageLink<Void>("myAccessTokens", MyAccessTokensPage.class));
		if (getPage() instanceof MyAccessTokensPage)
			item.add(AttributeAppender.append("class", "active"));

		userInfo.add(item = new ViewStateAwarePageLink<Void>("myTwoFactorAuthentication", MyTwoFactorAuthenticationPage.class));
		if (getPage() instanceof MyTwoFactorAuthenticationPage)
			item.add(AttributeAppender.append("class", "active"));

		if (!SecurityUtils.getPrevUserId().equals(0L)) {
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

		add(newVersionStatusBehavior = new AbstractPostAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				String newVersionStatus = RequestCycle.get().getRequest().getPostParameters().getParameterValue("newVersionStatus").toString();
				getUpdateCheckManager().cacheNewVersionStatus(newVersionStatus);
			}
		});
	}

	private SubscriptionManager getSubscriptionManager() {
		return OneDev.getInstance(SubscriptionManager.class);
	}

	private AlertManager getAlertManager() {
		return OneDev.getInstance(AlertManager.class);
	}

	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}

	@Override
	protected boolean isPermitted() {
		return getLoginUser() != null || getSettingManager().getSecuritySetting().isEnableAnonymousAccess();
	}

	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	private UpdateCheckManager getUpdateCheckManager() {
		return OneDev.getInstance(UpdateCheckManager.class);
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
