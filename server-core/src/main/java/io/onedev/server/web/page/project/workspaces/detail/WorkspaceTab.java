package io.onedev.server.web.page.project.workspaces.detail;

import io.onedev.server.web.component.tabbable.PageTab;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class WorkspaceTab extends PageTab {

	public WorkspaceTab(IModel<String> titleModel, IModel<String> iconModel, Class<? extends Page> mainPageClass, PageParameters mainPageParams) {
		super(titleModel, iconModel, mainPageClass, mainPageParams);
	}

}
