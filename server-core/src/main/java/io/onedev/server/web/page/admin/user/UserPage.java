package io.onedev.server.web.page.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.avatar.UserAvatar;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.authorization.UserAuthorizationsPage;
import io.onedev.server.web.page.admin.user.avatar.UserAvatarPage;
import io.onedev.server.web.page.admin.user.buildsetting.UserBuildSettingPage;
import io.onedev.server.web.page.admin.user.buildsetting.UserJobSecretsPage;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.page.admin.user.password.UserPasswordPage;
import io.onedev.server.web.page.admin.user.profile.UserProfilePage;
import io.onedev.server.web.page.admin.user.webhook.UserWebHooksPage;

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
			throw new OneException("System user is not accessible");
		
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
		
		add(new SideBar("userSidebar", null) {

			@Override
			protected Component newHead(String componentId) {
				Fragment fragment = new Fragment(componentId, "sidebarHeadFrag", UserPage.this);
				User user = userModel.getObject();
				fragment.add(new UserAvatar("avatar", user)
						.add(AttributeAppender.append("title", user.getDisplayName())));
				fragment.add(new Label("name", user.getDisplayName()));
				return fragment;
			}
			
			@Override
			protected List<? extends Tab> newTabs() {
				return UserPage.this.newTabs();
			}
			
		});
	}
	
	private List<? extends Tab> newTabs() {
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new UserTab("Profile", "fa fa-fw fa-list-alt", UserProfilePage.class));
		tabs.add(new UserTab("Edit Avatar", "fa fa-fw fa-picture-o", UserAvatarPage.class));
			
		tabs.add(new UserTab("Change Password", "fa fa-fw fa-key", UserPasswordPage.class));
		tabs.add(new UserTab("Belonging Groups", "fa fa-fw fa-group", UserMembershipsPage.class));
		tabs.add(new UserTab("Authorized Projects", "fa fa-fw fa-ext fa-repo", UserAuthorizationsPage.class));
		tabs.add(new UserTab("Build Setting", "fa fa-fw fa-cubes", UserJobSecretsPage.class, UserBuildSettingPage.class));
		tabs.add(new UserTab("Web Hooks", "fa fa-fw fa-volume-up", UserWebHooksPage.class, UserWebHooksPage.class));
		
		return tabs;
	}

	@Override
	protected void onDetach() {
		userModel.detach();
		
		super.onDetach();
	}
	
	@Override
	protected Component newNavContext(String componentId) {
		Fragment fragment = new Fragment(componentId, "navContextFrag", this);
		DropdownLink link = new DropdownLink("dropdown", AlignPlacement.bottom(15)) {

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeAppender.append("class", "nav-context-dropdown user-nav-context-dropdown"));
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "navContextDropdownFrag", UserPage.this);
				fragment.add(new Tabbable("menu", newTabs()));
				return fragment;
			}
			
		};
		link.add(new UserAvatar("avatar", getUser()));
		link.add(new Label("name", getUser().getDisplayName()));
		fragment.add(link);
		
		return fragment;
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

}
