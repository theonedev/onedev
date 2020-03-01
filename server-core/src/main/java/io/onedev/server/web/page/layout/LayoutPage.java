package io.onedev.server.web.page.layout;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.Plugin;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.OneWebApplication;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.groovyscript.GroovyScriptListPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.page.admin.group.create.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.IssueFieldListPage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import io.onedev.server.web.page.admin.jobexecutor.JobExecutorPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.role.NewRolePage;
import io.onedev.server.web.page.admin.role.RoleDetailPage;
import io.onedev.server.web.page.admin.role.RoleListPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.ssh.SshSettingPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.admin.user.create.NewUserPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.page.my.avatar.MyAvatarPage;
import io.onedev.server.web.page.my.buildsetting.MyBuildSettingPage;
import io.onedev.server.web.page.my.buildsetting.MyJobSecretsPage;
import io.onedev.server.web.page.my.password.MyPasswordPage;
import io.onedev.server.web.page.my.profile.MyProfilePage;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;
import io.onedev.server.web.page.my.webhook.MyWebHooksPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {
    
	public LayoutPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User loginUser = getLoginUser();
		
		add(newNavContext("navContext"));
		
		UICustomization customization = OneDev.getInstance(UICustomization.class);
		
		RepeatingView tabsView = new RepeatingView("navTabs");		
		for (MainTab tab: customization.getMainTabs()) {
			if (tab.isAuthorized()) {
				WebMarkupContainer tabContainer = new WebMarkupContainer(tabsView.newChildId());
				tabContainer.add(tab.render("tab"));
				if (tab.isActive((LayoutPage) getPage()))
					tabContainer.add(AttributeAppender.append("class", "active"));
				tabsView.add(tabContainer);
			}
		}
		add(tabsView);
		
		WebMarkupContainer administrationContainer = new WebMarkupContainer("navAdministration");
		WebMarkupContainer item;

		administrationContainer.add(item = new WebMarkupContainer("userManagement"));
		item.add(new ViewStateAwarePageLink<Void>("link", UserListPage.class));
		if (getPage() instanceof UserListPage || getPage() instanceof NewUserPage || getPage() instanceof UserPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("roleManagement"));
		item.add(new ViewStateAwarePageLink<Void>("link", RoleListPage.class));
		if (getPage() instanceof RoleListPage || getPage() instanceof NewRolePage || getPage() instanceof RoleDetailPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("groupManagement"));
		item.add(new ViewStateAwarePageLink<Void>("link", GroupListPage.class));
		if (getPage() instanceof GroupListPage || getPage() instanceof NewGroupPage || getPage() instanceof GroupPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("securitySetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", SecuritySettingPage.class));
		if (getPage() instanceof SecuritySettingPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("externelAuthentication"));
		item.add(new ViewStateAwarePageLink<Void>("link", AuthenticatorPage.class));
		if (getPage() instanceof AuthenticatorPage)
			item.add(AttributeAppender.append("class", "active"));

	    administrationContainer.add(item = new WebMarkupContainer("sshSetting"));
	    item.add(new ViewStateAwarePageLink<Void>("link", SshSettingPage.class));
	    if (getPage() instanceof SshSettingPage)
	        item.add(AttributeAppender.append("class", "active"));
		item.setVisible(OneWebApplication.get().isSshEnabled());
	    
		administrationContainer.add(item = new WebMarkupContainer("jobExecutors"));
		item.add(new ViewStateAwarePageLink<Void>("link", JobExecutorPage.class));
		if (getPage() instanceof JobExecutorPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("groovyScripts"));
		item.add(new ViewStateAwarePageLink<Void>("link", GroovyScriptListPage.class));
		if (getPage() instanceof GroovyScriptListPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("systemSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", SystemSettingPage.class));
		if (getPage() instanceof SystemSettingPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("issueSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", IssueFieldListPage.class));
		if (getPage() instanceof IssueSettingPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("mailSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", MailSettingPage.class));
		if (getPage() instanceof MailSettingPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("databaseBackup"));
		item.add(new ViewStateAwarePageLink<Void>("link", DatabaseBackupPage.class));
		if (getPage() instanceof DatabaseBackupPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("serverLog"));
		item.add(new ViewStateAwarePageLink<Void>("link", ServerLogPage.class));
		if (getPage() instanceof ServerLogPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("serverInformation"));
		item.add(new ViewStateAwarePageLink<Void>("link", ServerInformationPage.class));
		if (getPage() instanceof ServerInformationPage)
			item.add(AttributeAppender.append("class", "active"));
		
		if (getPage() instanceof AdministrationPage) 
			administrationContainer.add(AttributeAppender.append("class", "active"));
		administrationContainer.setVisible(SecurityUtils.isAdministrator());
		add(administrationContainer);
		
		Plugin product = AppLoader.getProduct();
		add(new Label("productVersion", product.getVersion()));
		add(new ExternalLink("docLink", OneDev.getInstance().getDocRoot() + "/"));
		
		WebMarkupContainer notSignedInContainer = new WebMarkupContainer("notSignedIn");
		notSignedInContainer.add(new Link<Void>("signIn") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		});
		
		boolean enableSelfRegister = OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableSelfRegister();
		notSignedInContainer.add(new ViewStateAwarePageLink<Void>("signUp", RegisterPage.class).setVisible(enableSelfRegister));
		notSignedInContainer.setVisible(loginUser == null);
		add(notSignedInContainer);
		
		WebMarkupContainer signedInContainer = new WebMarkupContainer("navSignedIn");
		if (loginUser != null) {
			signedInContainer.add(new UserAvatar("avatar", loginUser));
			signedInContainer.add(new Label("name", loginUser.getDisplayName()));
			signedInContainer.add(new Label("header", loginUser.getDisplayName()));
		} else {
			signedInContainer.add(new WebMarkupContainer("avatar"));
			signedInContainer.add(new WebMarkupContainer("name"));
			signedInContainer.add(new WebMarkupContainer("header"));
		}
		
		signedInContainer.add(item = new WebMarkupContainer("myProfile"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyProfilePage.class));
		if (getPage() instanceof MyProfilePage)
			item.add(AttributeAppender.append("class", "active"));
		
		signedInContainer.add(item = new WebMarkupContainer("myAvatar"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyAvatarPage.class));
		if (getPage() instanceof MyAvatarPage)
			item.add(AttributeAppender.append("class", "active"));
		
		signedInContainer.add(item = new WebMarkupContainer("myPassword"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyPasswordPage.class));
		if (getPage() instanceof MyPasswordPage)
			item.add(AttributeAppender.append("class", "active"));

		signedInContainer.add(item = new WebMarkupContainer("mySshKeys"));
		item.add(new ViewStateAwarePageLink<Void>("link", MySshKeysPage.class));
		if (getPage() instanceof MySshKeysPage)
		    item.add(AttributeAppender.append("class", "active"));
		item.setVisible(OneWebApplication.get().isSshEnabled());
		
		signedInContainer.add(item = new WebMarkupContainer("myBuildSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyJobSecretsPage.class));
		if (getPage() instanceof MyBuildSettingPage)
			item.add(AttributeAppender.append("class", "active"));

		signedInContainer.add(item = new WebMarkupContainer("myWebHooks"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyWebHooksPage.class));
		if (getPage() instanceof MyWebHooksPage)
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
			signedInContainer.add(signOutLink);
		} else {
			ViewStateAwarePageLink<Void> signOutLink = new ViewStateAwarePageLink<Void>("signOut", LogoutPage.class); 
			signOutLink.add(new Label("label", "Sign Out"));
			signedInContainer.add(signOutLink);
		}

		signedInContainer.setVisible(loginUser != null);
		
		if (getPage() instanceof MyPage)
			signedInContainer.add(AttributeAppender.append("class", "active"));
		
		add(signedInContainer);
	}

	@Override
	protected boolean isPermitted() {
		return getLoginUser() != null || OneDev.getInstance(SettingManager.class).getSecuritySetting().isEnableAnonymousAccess();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new LayoutResourceReference()));
	}

	protected Component newNavContext(String componentId) {
		return new WebMarkupContainer(componentId);
	}
	
}
