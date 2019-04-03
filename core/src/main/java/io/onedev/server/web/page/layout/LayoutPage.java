package io.onedev.server.web.page.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.launcher.loader.Plugin;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.userident.UserIdent;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.authenticator.AuthenticatorPage;
import io.onedev.server.web.page.admin.databasebackup.DatabaseBackupPage;
import io.onedev.server.web.page.admin.group.GroupListPage;
import io.onedev.server.web.page.admin.group.GroupPage;
import io.onedev.server.web.page.admin.group.NewGroupPage;
import io.onedev.server.web.page.admin.issuesetting.GlobalIssueSettingPage;
import io.onedev.server.web.page.admin.issuesetting.IssueFieldListPage;
import io.onedev.server.web.page.admin.jobexecutors.JobExecutorPage;
import io.onedev.server.web.page.admin.mailsetting.MailSettingPage;
import io.onedev.server.web.page.admin.securitysetting.SecuritySettingPage;
import io.onedev.server.web.page.admin.serverinformation.ServerInformationPage;
import io.onedev.server.web.page.admin.serverlog.ServerLogPage;
import io.onedev.server.web.page.admin.systemsetting.SystemSettingPage;
import io.onedev.server.web.page.admin.user.NewUserPage;
import io.onedev.server.web.page.admin.user.UserListPage;
import io.onedev.server.web.page.admin.user.UserPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.my.MyAvatarPage;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.page.my.MyPasswordPage;
import io.onedev.server.web.page.my.MyProfilePage;
import io.onedev.server.web.page.my.MyTokenPage;
import io.onedev.server.web.page.project.NewProjectPage;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.security.LoginPage;
import io.onedev.server.web.page.security.LogoutPage;
import io.onedev.server.web.page.security.RegisterPage;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {
	
	private final ClientProperties clientProperties;
	
	public LayoutPage(PageParameters params) {
		super(params);
		clientProperties = ((WebClientInfo)getSession().getClientInfo()).getProperties();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		User loginUser = getLoginUser();
		
		add(newNavContext("navContext"));
		
		WebMarkupContainer projectsContainer = new WebMarkupContainer("navProjects");
		projectsContainer.add(new ViewStateAwarePageLink<Void>("link", ProjectListPage.class));
		if (getPage() instanceof ProjectListPage || getPage() instanceof ProjectPage || getPage() instanceof NewProjectPage) 
			projectsContainer.add(AttributeAppender.append("class", "active"));
		add(projectsContainer);
		
		RepeatingView contributionsView = new RepeatingView("navContributions");		
		List<MainNavContribution> contributions = new ArrayList<>();
		for (MainNavContribution contribution: OneDev.getExtensions(MainNavContribution.class)) {
			if (contribution.isAuthorized())
				contributions.add(contribution);
		}
		Collections.sort(contributions, new Comparator<MainNavContribution>() {

			@Override
			public int compare(MainNavContribution o1, MainNavContribution o2) {
				return o1.getOrder() - o2.getOrder();
			}
			
		});
		for (MainNavContribution contribution: contributions) {
			WebMarkupContainer contributionContainer = new WebMarkupContainer(contributionsView.newChildId());
			Link<Void> link = new ViewStateAwarePageLink<Void>("link", contribution.getPageClass());
			link.add(new Label("label", contribution.getLabel()));
			contributionContainer.add(link);
			if (contribution.isActive((LayoutPage) getPage()))
				contributionContainer.add(AttributeAppender.append("class", "active"));
			contributionsView.add(contributionContainer);
		}
		add(contributionsView);
		
		WebMarkupContainer administrationContainer = new WebMarkupContainer("navAdministration");
		WebMarkupContainer item;

		administrationContainer.add(item = new WebMarkupContainer("userManagement"));
		item.add(new ViewStateAwarePageLink<Void>("link", UserListPage.class));
		if (getPage() instanceof UserListPage || getPage() instanceof NewUserPage || getPage() instanceof UserPage)
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
		
		administrationContainer.add(item = new WebMarkupContainer("jobExecutors"));
		item.add(new ViewStateAwarePageLink<Void>("link", JobExecutorPage.class));
		if (getPage() instanceof JobExecutorPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("systemSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", SystemSettingPage.class));
		if (getPage() instanceof SystemSettingPage)
			item.add(AttributeAppender.append("class", "active"));
		
		administrationContainer.add(item = new WebMarkupContainer("issueSetting"));
		item.add(new ViewStateAwarePageLink<Void>("link", IssueFieldListPage.class));
		if (getPage() instanceof GlobalIssueSettingPage)
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
		signedInContainer.add(new UserAvatar("avatar", UserIdent.of(UserFacade.of(loginUser))));
		signedInContainer.add(new Label("name", loginUser!=null?loginUser.getDisplayName():""));
		signedInContainer.add(new Label("header", loginUser!=null?loginUser.getDisplayName():""));
		
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
		
		signedInContainer.add(item = new WebMarkupContainer("myAccessToken"));
		item.add(new ViewStateAwarePageLink<Void>("link", MyTokenPage.class));
		if (getPage() instanceof MyTokenPage)
			item.add(AttributeAppender.append("class", "active"));
		
		signedInContainer.add(new ViewStateAwarePageLink<Void>("signOut", LogoutPage.class));
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
	
	public ClientProperties getClientProperties() {
		return clientProperties;
	}
	
}
