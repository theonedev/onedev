package io.onedev.server.web.page.admin.group;

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
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.sidebar.SideBar;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.group.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.group.membership.GroupMembershipsPage;
import io.onedev.server.web.page.admin.group.profile.GroupProfilePage;

@SuppressWarnings("serial")
public abstract class GroupPage extends AdministrationPage {
	
	private static final String PARAM_GROUP = "group";
	
	protected final IModel<Group> groupModel;
	
	public GroupPage(PageParameters params) {
		super(params);
		
		String groupIdString = params.get(PARAM_GROUP).toString();
		if (StringUtils.isBlank(groupIdString))
			throw new RestartResponseException(GroupListPage.class);
		
		Long groupId = Long.valueOf(groupIdString);
		
		groupModel = new LoadableDetachableModel<Group>() {

			@Override
			protected Group load() {
				return OneDev.getInstance(GroupManager.class).load(groupId);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SideBar("groupSidebar", null) {

			@Override
			protected Component newHead(String componentId) {
				String content = "<i class='fa fa-group'></i> " + HtmlEscape.escapeHtml5(getGroup().getName()); 
				return new Label(componentId, content).setEscapeModelStrings(false);
			}

			@Override
			protected List<? extends Tab> newTabs() {
				return GroupPage.this.newTabs();
			}
			
		});
	}
	
	private List<? extends Tab> newTabs() {
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new GroupTab("Profile", "fa fa-fw fa-list-alt", GroupProfilePage.class));
		tabs.add(new GroupTab("Members", "fa fa-fw fa-user", GroupMembershipsPage.class));
		if (SecurityUtils.isAdministrator() && !getGroup().isAdministrator())
			tabs.add(new GroupTab("Authorized Projects", "fa fa-fw fa-ext fa-repo", GroupAuthorizationsPage.class));
		return tabs;
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
				Fragment fragment = new Fragment(id, "navContextDropdownFrag", GroupPage.this);
				fragment.add(new Tabbable("menu", newTabs()));
				return fragment;
			}
			
		};
		link.add(new Label("name", getGroup().getName()));
		fragment.add(link);
		
		return fragment;
	}	
	
	@Override
	protected void onDetach() {
		groupModel.detach();
		
		super.onDetach();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}
	
	public Group getGroup() {
		return groupModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	public static PageParameters paramsOf(Group group) {
		PageParameters params = new PageParameters();
		params.add(PARAM_GROUP, group.getId());
		return params;
	}

}
