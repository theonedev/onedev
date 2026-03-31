package io.onedev.server.web.page.user.workspacedata;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.workspacedata.WorkspaceDataPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserWorkspaceDataPage extends UserPage {

	public UserWorkspaceDataPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new WorkspaceDataPanel("workspaceData", getUser().getId()));
	}

}
