package io.onedev.server.web.page.admin.user;

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
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.accesstoken.UserAccessTokenPage;
import io.onedev.server.web.page.admin.user.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.password.UserPasswordPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.admin.user.ssh.UserSshKeysPage;

@SuppressWarnings("serial")
public abstract class UserPage extends AdministrationPage {
	
	private static final String PARAM_USER = "user";
	
	protected final IModel<User> userModel;
	
	public UserPage(PageParameters params) {
		super(params);
		
		String userIdString = params.get(PARAM_USER).toString();
		if (StringUtils.isBlank(userIdString))
			throw new RestartResponseException(UserListPage.class);
		
		Long userId = Long.valueOf(userIdString);
		if (userId == User.SYSTEM_ID)
			throw new ExplicitException("System user is not accessible");
		
		userModel = new LoadableDetachableModel<User>() {

			@Override
			protected User load() {
				return OneDev.getInstance(UserManager.class).load(userId);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new UserTab("Profile", "profile", UserProfilePage.class));
		tabs.add(new UserTab("Edit Avatar", "avatar", UserAvatarPage.class));
			
		tabs.add(new UserTab("Change Password", "password", UserPasswordPage.class));
		tabs.add(new UserTab("Belonging Groups", "group", UserMembershipsPage.class));
		tabs.add(new UserTab("Authorized Projects", "project", UserAuthorizationsPage.class));
		tabs.add(new UserTab("SSH Keys", "key", UserSshKeysPage.class));
		tabs.add(new UserTab("Access Token", "token", UserAccessTokenPage.class));
		
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
	
	public User getUser() {
		return userModel.getObject();
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
