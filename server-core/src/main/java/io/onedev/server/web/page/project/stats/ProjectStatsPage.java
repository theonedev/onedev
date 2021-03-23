package io.onedev.server.web.page.project.stats;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ProjectStatsPage extends ProjectPage {

	public ProjectStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectStatsResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return "Statistics - " + getProject().getName();
	}
	
}
