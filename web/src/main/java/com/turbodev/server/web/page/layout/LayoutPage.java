package com.turbodev.server.web.page.layout;

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

import com.turbodev.launcher.loader.AppLoader;
import com.turbodev.launcher.loader.Plugin;
import com.turbodev.utils.license.LicenseDetail;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.dao.EntityCriteria;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.ComponentRenderer;
import com.turbodev.server.web.behavior.TooltipBehavior;
import com.turbodev.server.web.component.avatar.AvatarLink;
import com.turbodev.server.web.component.floating.AlignPlacement;
import com.turbodev.server.web.component.floating.FloatingPanel;
import com.turbodev.server.web.component.link.DropdownLink;
import com.turbodev.server.web.component.link.ViewStateAwarePageLink;
import com.turbodev.server.web.page.base.BasePage;
import com.turbodev.server.web.page.dashboard.DashboardPage;
import com.turbodev.server.web.page.security.LoginPage;
import com.turbodev.server.web.page.security.LogoutPage;
import com.turbodev.server.web.page.security.RegisterPage;
import com.turbodev.server.web.page.user.TaskListPage;
import com.turbodev.server.web.page.user.UserProfilePage;
import com.turbodev.server.web.websocket.PageDataChanged;
import com.turbodev.server.web.websocket.TaskChangedRegion;
import com.turbodev.server.web.websocket.WebSocketRegion;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

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

		int userCount = TurboDev.getInstance(UserManager.class).count(EntityCriteria.of(User.class));
		int licenseLimit = LicenseDetail.FREE_LICENSE_USERS;
		LicenseDetail license = TurboDev.getInstance(ConfigManager.class).getLicense();
		if (license != null && license.getRemainingDays()>=0)
			licenseLimit += license.getLicensedUsers();
		if (userCount > licenseLimit) {
			String tooltip = String.format(""
					+ "Git push is disabled as number of users (%d) in system exceeds license limit (%d).", 
					userCount, licenseLimit);
			TooltipBehavior tooltipBehavior = new TooltipBehavior(Model.of(tooltip), 
					new TooltipConfig().withPlacement(Placement.bottom)); 
			head.add(new WebMarkupContainer("pushDisabled").add(tooltipBehavior));
		} else {
			head.add(new WebMarkupContainer("pushDisabled").setVisible(false));
		}
		head.add(new ExternalLink("docLink", TurboDev.getInstance().getDocLink() + "/readme.md"));
		
		User user = getLoginUser();
		boolean signedIn = user != null;

		head.add(new Link<Void>("login") {

			@Override
			public void onClick() {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
			
		}.setVisible(!signedIn));
		
		boolean enableSelfRegister = TurboDev.getInstance(ConfigManager.class).getSecuritySetting().isEnableSelfRegister();
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
	
	protected boolean isFootVisible() {
		return true;
	}
	
	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		if (getLoginUser() != null) {
			regions.add(new TaskChangedRegion(getLoginUser().getId()));
		}
		return regions;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessPublic();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new LayoutResourceReference()));
	}
	
}
