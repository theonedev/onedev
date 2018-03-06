package io.onedev.server.web.page.group;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ComponentRenderer;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.sidebar.SidebarPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.util.model.EntityModel;

@SuppressWarnings("serial")
public abstract class GroupPage extends LayoutPage {
	
	private static final String PARAM_GROUP = "group";
	
	protected final IModel<Group> groupModel;
	
	public GroupPage(PageParameters params) {
		super(params);
		
		Long groupId = params.get(PARAM_GROUP).toLong();
		
		Group group = OneDev.getInstance(GroupManager.class).load(groupId);
		groupModel = new EntityModel<Group>(group);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SidebarPanel("sidebar", "group.miniSidebar") {

			@Override
			protected List<? extends Tab> newTabs() {
				List<PageTab> tabs = new ArrayList<>();
				
				tabs.add(new GroupTab("Profile", "fa fa-fw fa-list-alt", GroupProfilePage.class));
				tabs.add(new GroupTab("Members", "fa fa-fw fa-user", GroupMembershipsPage.class));
				if (SecurityUtils.isAdministrator() && !getGroup().isAdministrator())
					tabs.add(new GroupTab("Authorized Projects", "fa fa-fw fa-ext fa-repo", GroupAuthorizationsPage.class));
				return tabs;
			}
			
		});
	}

	@Override
	protected void onDetach() {
		groupModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupResourceReference()));
	}
	
	public Group getGroup() {
		return groupModel.getObject();
	}
	
	public static PageParameters paramsOf(Group group) {
		PageParameters params = new PageParameters();
		params.add(PARAM_GROUP, group.getId());
		return params;
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		List<ComponentRenderer> breadcrumbs = super.getBreadcrumbs();

		breadcrumbs.add(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new ViewStateAwarePageLink<Void>(componentId, GroupListPage.class) {

					@Override
					public IModel<?> getBody() {
						return Model.of("Groups");
					}
					
				};
			}
			
		});

		breadcrumbs.add(new ComponentRenderer() {
			
			@Override
			public Component render(String componentId) {
				Label label = new Label(componentId, getGroup().getName());
				label.add(AttributeAppender.append("class", "name"));
				label.add(AttributeAppender.append("title", getGroup().getName()));
				return label;
			}
			
		});
		
		return breadcrumbs;
	}
	
}
