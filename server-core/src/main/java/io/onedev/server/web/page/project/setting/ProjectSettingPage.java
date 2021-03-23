package io.onedev.server.web.page.project.setting;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ProjectSettingPage extends ProjectPage {

	public ProjectSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canManage(getProject());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new ProjectSettingResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return "Settings - " + getProject().getName();
	}
	
}
