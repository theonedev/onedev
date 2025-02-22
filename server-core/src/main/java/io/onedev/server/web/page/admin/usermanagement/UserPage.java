package io.onedev.server.web.page.admin.usermanagement;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.usermanagement.accesstoken.UserAccessTokensPage;
import io.onedev.server.web.page.admin.usermanagement.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.usermanagement.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.usermanagement.emailaddresses.UserEmailAddressesPage;
import io.onedev.server.web.page.admin.usermanagement.gpgkeys.UserGpgKeysPage;
import io.onedev.server.web.page.admin.usermanagement.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.usermanagement.password.UserPasswordPage;
import io.onedev.server.web.page.admin.usermanagement.preferences.UserPreferencesPage;
import io.onedev.server.web.page.admin.usermanagement.profile.UserProfilePage;
import io.onedev.server.web.page.admin.usermanagement.sshkeys.UserSshKeysPage;
import io.onedev.server.web.page.admin.usermanagement.twofactorauthentication.UserTwoFactorAuthenticationPage;
import io.onedev.server.web.util.UserAware;

public abstract class UserPage extends AdministrationPage implements UserAware {
	
	public static final String PARAM_USER = "user";
	
	protected final IModel<User> userModel;
	
	public UserPage(PageParameters params) {
		super(params);
		
		String userIdString = params.get(PARAM_USER).toString();
		if (StringUtils.isBlank(userIdString))
			throw new RestartResponseException(UserListPage.class);
		
		Long userId = Long.valueOf(userIdString);
		Preconditions.checkArgument(userId > 0);
		
		userModel = new LoadableDetachableModel<>() {

			@Override
			protected User load() {
				return getUserManager().load(userId);
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		var params = paramsOf(getUser());
		tabs.add(new PageTab(Model.of("Profile"), Model.of("profile"), UserProfilePage.class, params));
		tabs.add(new PageTab(Model.of("Email Addresses"), Model.of("mail"), UserEmailAddressesPage.class, params));
		tabs.add(new PageTab(Model.of("Edit Avatar"), Model.of("avatar"), UserAvatarPage.class, params));
			
		tabs.add(new PageTab(Model.of("Password"), Model.of("password"), UserPasswordPage.class, params));
		tabs.add(new PageTab(Model.of("Belonging Groups"), Model.of("group"), UserMembershipsPage.class, params));
		tabs.add(new PageTab(Model.of("Authorized Projects"), Model.of("project"), UserAuthorizationsPage.class, params));
		if (OneDev.getInstance(ServerConfig.class).getSshPort() != 0)
			tabs.add(new PageTab(Model.of("SSH Keys"), Model.of("key"), UserSshKeysPage.class, params));
		tabs.add(new PageTab(Model.of("GPG Keys"), Model.of("key"), UserGpgKeysPage.class, params));
		tabs.add(new PageTab(Model.of("Access Tokens"), Model.of("token"), UserAccessTokensPage.class, params));
		if (getUser().isEnforce2FA())
			tabs.add(new PageTab(Model.of("Two-factor Authentication"), Model.of("shield"), UserTwoFactorAuthenticationPage.class, params));
		tabs.add(new PageTab(Model.of("Preferences"), Model.of("sliders"), UserPreferencesPage.class, params));
		
		add(new Tabbable("userTabs", tabs));
	}
	
	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}
	
	@Override
	public User getUser() {
		return userModel.getObject();
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	public static PageParameters paramsOf(User user) {
		PageParameters params = new PageParameters();
		params.add(PARAM_USER, user.getId());
		return params;
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("users", UserListPage.class));
		fragment.add(new Label("userName", getUser().getDisplayName()));
		return fragment;
	}

}
