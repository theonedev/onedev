package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

public abstract class CodeStatsPage extends ProjectPage {

	public CodeStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var tabs = new ArrayList<PageTab>();
		var params = paramsOf(getProject());
		tabs.add(new PageTab(Model.of(_T("Contributions")), CodeContribsPage.class, params));
		tabs.add(new PageTab(Model.of(_T("Source Lines")), SourceLinesPage.class, params));
		add(new Tabbable("tabs", tabs));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CodeStatsResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return _T("Code Statistics") + " - " + getProject().getPath();
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project))
			return new ViewStateAwarePageLink<>(componentId, getPageClass(), paramsOf(project.getId()));
		else
			return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}

}