package com.gitplex.server.web.page.group;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.Group;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.component.tabbable.PageTab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.util.model.EntityModel;

@SuppressWarnings("serial")
public abstract class GroupPage extends LayoutPage {
	
	private static final String PARAM_GROUP = "group";
	
	protected final IModel<Group> groupModel;
	
	public GroupPage(PageParameters params) {
		super(params);
		
		Long groupId = params.get(PARAM_GROUP).toLong();
		
		Group group = GitPlex.getInstance(GroupManager.class).load(groupId);
		groupModel = new EntityModel<Group>(group);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		tabs.add(new GroupTab("Profile", "fa fa-fw fa-list-alt", GroupProfilePage.class));
		tabs.add(new GroupTab("Members", "fa fa-fw fa-user", GroupMembershipsPage.class));
		if (SecurityUtils.isAdministrator() && !getGroup().isAdministrator())
			tabs.add(new GroupTab("Authorizations", "fa fa-fw fa-lock", GroupAuthorizationsPage.class));
		
		add(new Tabbable("groupTabs", tabs));
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
	protected Component newContextHead(String componentId) {
		return new Label(componentId, "Group - " + getGroup().getName());
	}

}
