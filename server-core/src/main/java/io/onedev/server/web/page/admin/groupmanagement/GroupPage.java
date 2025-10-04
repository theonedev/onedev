package io.onedev.server.web.page.admin.groupmanagement;

import static io.onedev.server.web.translation.Translation._T;

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

import io.onedev.server.OneDev;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.groupmanagement.authorization.GroupAuthorizationsPage;
import io.onedev.server.web.page.admin.groupmanagement.membership.GroupMembershipsPage;
import io.onedev.server.web.page.admin.groupmanagement.profile.GroupProfilePage;

public abstract class GroupPage extends AdministrationPage {
	
	public static final String PARAM_GROUP = "group";
	
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
				return OneDev.getInstance(GroupService.class).load(groupId);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<PageTab> tabs = new ArrayList<>();
		
		var params = paramsOf(getGroup());
		tabs.add(new PageTab(Model.of(_T("Basic Settings")), Model.of("info"), GroupProfilePage.class, params));
		tabs.add(new PageTab(Model.of(_T("Members")), Model.of("members"), GroupMembershipsPage.class, params));
		tabs.add(new PageTab(Model.of(_T("Authorized Projects")), Model.of("project"), GroupAuthorizationsPage.class, params));
		
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

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("groups", GroupListPage.class));
		fragment.add(new Label("groupName", getGroup().getName()));
		return fragment;
	}

}
