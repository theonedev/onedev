package com.gitplex.server.web.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.component.avatar.Avatar;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.tabbable.PageTab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.util.model.EntityModel;
import com.gitplex.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class UserPage extends LayoutPage {
	
	private static final String PARAM_USER = "user";
	
	protected final IModel<User> userModel;
	
	public UserPage(PageParameters params) {
		super(params);
		
		User user = GitPlex.getInstance(UserManager.class).load(params.get(PARAM_USER).toLong());
		userModel = new EntityModel<User>(user);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ViewStateAwarePageLink<Void> avatarLink = new ViewStateAwarePageLink<Void>("avatar", 
				AvatarEditPage.class, AvatarEditPage.paramsOf(getUser())) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!isEnabled())
					tag.setName("span");
			}
			
		};
		if (!SecurityUtils.canManage(getUser())) {
			avatarLink.setEnabled(false);
		}
		add(avatarLink);
		avatarLink.add(new Avatar("avatar", userModel.getObject()));
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new UserTab("Profile", "fa fa-fw fa-list-alt", UserProfilePage.class));
		if (SecurityUtils.canManage(getUser())) {
			tabs.add(new UserTab("Edit Avatar", "fa fa-fw fa-picture-o", AvatarEditPage.class));
			
			if (StringUtils.isNotBlank(getUser().getPassword()))
				tabs.add(new UserTab("Change Password", "fa fa-fw fa-key", PasswordEditPage.class));
			tabs.add(new UserTab("Access Token", "fa fa-fw fa-key", TokenGeneratePage.class));
		}
		tabs.add(new UserTab("Belonging Groups", "fa fa-fw fa-group", UserMembershipsPage.class));
		if (SecurityUtils.isAdministrator()) 
			tabs.add(new UserTab("Authorized Projects", "fa fa-fw fa-ext fa-repo", UserAuthorizationsPage.class));
		if (SecurityUtils.canManage(getUser()))
			tabs.add(new UserTab("Tasks", "fa fa-fw fa-bell-o", TaskListPage.class));
		
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
		response.render(CssHeaderItem.forReference(new UserResourceReference()));
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
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();

		if (SecurityUtils.isAdministrator()) {
			breadcrumbs.add(new ComponentRenderer() {
	
				@Override
				public Component render(String componentId) {
					return new ViewStateAwarePageLink<Void>(componentId, UserListPage.class) {
	
						@Override
						public IModel<?> getBody() {
							return Model.of("Users");
						}
						
					};
				}
				
			});
		}
		
		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				if (SecurityUtils.isAdministrator()) {
					return new Label(componentId, getUser().getDisplayName());
				} else {
					return new Label(componentId, "User (" + getUser().getDisplayName() + ")");
				}
			}
			
		});
		
		return breadcrumbs;
	}

}
