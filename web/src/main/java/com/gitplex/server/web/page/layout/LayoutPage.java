package com.gitplex.server.web.page.layout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.launcher.loader.Plugin;
import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.model.User;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.component.avatar.AvatarLink;
import com.gitplex.server.web.component.floating.AlignPlacement;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.base.BasePage;
import com.gitplex.server.web.page.dashboard.DashboardPage;
import com.gitplex.server.web.page.project.NewProjectPage;
import com.gitplex.server.web.page.security.LoginPage;
import com.gitplex.server.web.page.security.LogoutPage;
import com.gitplex.server.web.page.security.RegisterPage;
import com.gitplex.server.web.page.user.TaskListPage;
import com.gitplex.server.web.page.user.UserProfilePage;
import com.gitplex.server.web.websocket.PageDataChanged;
import com.gitplex.server.web.websocket.TaskChangedRegion;
import com.gitplex.server.web.websocket.WebSocketRegion;

@SuppressWarnings("serial")
public abstract class LayoutPage extends BasePage {

	private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd";
	
	public LayoutPage() {
	}
	
	public LayoutPage(PageParameters params) {
		super(params);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer head = new WebMarkupContainer("mainHead");
		add(head);
		
		head.add(new ViewStateAwarePageLink<Void>("home", getApplication().getHomePage()));

		head.add(new ListView<ComponentRenderer>("breadcrumbs", getBreadcrumbs()) {

			@Override
			protected void populateItem(ListItem<ComponentRenderer> item) {
				item.add(item.getModelObject().render("nav"));
				item.add(new WebMarkupContainer("separator").setVisible(item.getIndex() != getList().size()-1));
			}
			
		});
		
		head.add(new ViewStateAwarePageLink<Void>("createProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});
		
		head.add(new ExternalLink("docLink", GitPlex.getInstance().getDocLink()));
		
		User user = getLoginUser();
		boolean signedIn = user != null;

		head.add(new Link<Void>("login") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		}.setVisible(!signedIn));
		
		boolean enableSelfRegister = GitPlex.getInstance(ConfigManager.class).getSecuritySetting().isEnableSelfRegister();
		head.add(new ViewStateAwarePageLink<Void>("register", RegisterPage.class).setVisible(!signedIn && enableSelfRegister));
		head.add(new ViewStateAwarePageLink<Void>("logout", LogoutPage.class).setVisible(signedIn));
		if (user != null) {
			head.add(new ViewStateAwarePageLink<Void>("tasks", TaskListPage.class, TaskListPage.paramsOf(user)) {
	
				@Override
				public void onEvent(IEvent<?> event) {
					super.onEvent(event);
					if (event.getPayload() instanceof PageDataChanged) {
						PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
						pageDataChanged.getHandler().add(this);
					}
				}
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(!getLoginUser().getRequestTasks().isEmpty());
				}
				
			}.setOutputMarkupPlaceholderTag(true));
		} else {
			head.add(new WebMarkupContainer("tasks").setVisible(false));
		}
		
		if (signedIn) {
			head.add(new AvatarLink("user", user));
			head.add(new DropdownLink("userMenuTrigger", new AlignPlacement(50, 100, 50, 0, 8)) {

				@Override
				protected Component newContent(String id, FloatingPanel dropdown) {
					Fragment fragment = new Fragment(id, "userMenuFrag", LayoutPage.this);

					fragment.add(new ViewStateAwarePageLink<Void>("profile", 
							UserProfilePage.class, UserProfilePage.paramsOf(user)));
					fragment.add(new ViewStateAwarePageLink<Void>("logout", LogoutPage.class));
					
					return fragment;
				}
				
			});
		} else {  
			head.add(new WebMarkupContainer("user").setVisible(false));
			head.add(new WebMarkupContainer("userMenuTrigger").setVisible(false));
			head.add(new WebMarkupContainer("userMenu").setVisible(false));
		}
		
		add(new WebMarkupContainer("mainFoot") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				Plugin product = AppLoader.getProduct();
				add(new Label("productVersion", product.getVersion()));
				add(new Label("releaseDate", new SimpleDateFormat(RELEASE_DATE_FORMAT).format(product.getDate())));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isFootVisible());
			}
			
		});
	}

	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = new ArrayList<>();
		
		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, DashboardPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Home");
					}
					
				};
			}
		});
		
		return breadcrumbs;
	};
	
	protected boolean isLoggedIn() {
		return getLoginUser() != null;
	}
	
	protected boolean isRemembered() {
		return SecurityUtils.getSubject().isRemembered();
	}
	
	protected boolean isAuthenticated() {
		return SecurityUtils.getSubject().isAuthenticated();
	}

	protected boolean isFootVisible() {
		return true;
	}
	
	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		if (isLoggedIn()) {
			regions.add(new TaskChangedRegion(getLoginUser().getId()));
		}
		return regions;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new LayoutResourceReference()));
	}
	
}
