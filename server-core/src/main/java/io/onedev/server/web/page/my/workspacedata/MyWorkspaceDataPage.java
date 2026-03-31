package io.onedev.server.web.page.my.workspacedata;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.workspacedata.WorkspaceDataPanel;
import io.onedev.server.web.page.my.MyPage;

public class MyWorkspaceDataPage extends MyPage {

	public MyWorkspaceDataPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new WorkspaceDataPanel("workspaceData", getUser().getId()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My Workspace Data"));
	}

}
