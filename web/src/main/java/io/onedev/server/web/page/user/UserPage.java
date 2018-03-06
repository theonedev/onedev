package io.onedev.server.web.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ComponentRenderer;
import io.onedev.server.web.component.avatar.Avatar;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sidebar.SidebarPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.model.EntityModel;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public abstract class UserPage extends LayoutPage {
	
	private static final String PARAM_USER = "user";
	
	protected final IModel<User> userModel;
	
	public UserPage(PageParameters params) {
		super(params);
		
		User user = OneDev.getInstance(UserManager.class).load(params.get(PARAM_USER).toLong());
		userModel = new EntityModel<User>(user);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SidebarPanel("sidebar", "user.miniSidebar") {

			@Override
			protected Component newHead(String componentId) {
				Fragment fragment = new Fragment(componentId, "sidebarHeadFrag", UserPage.this);
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
				avatarLink.add(new Avatar("avatar", userModel.getObject()));
				fragment.add(avatarLink);
				
				return fragment;
			}

			@Override
			protected List<? extends Tab> newTabs() {
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
				
				return tabs;
			}
			
		});
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
				Label label;
				if (SecurityUtils.isAdministrator()) {
					label = new Label(componentId, getUser().getDisplayName());
				} else {
					label = new Label(componentId, "User (" + getUser().getDisplayName() + ")");
				}
				label.add(AttributeAppender.append("class", "name"));
				label.add(AttributeAppender.append("title", getUser().getDisplayName()));
				return label;
			}
			
		});
		
		return breadcrumbs;
	}

}
