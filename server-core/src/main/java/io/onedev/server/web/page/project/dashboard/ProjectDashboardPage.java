package io.onedev.server.web.page.project.dashboard;

import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;

@SuppressWarnings("serial")
public class ProjectDashboardPage extends ProjectPage {

	public ProjectDashboardPage(PageParameters params) {
		super(params);
		
		PageProvider pageProvider;
		if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject()))
			pageProvider = new PageProvider(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject()));
		else if (getProject().isIssueManagement()) 
			pageProvider = new PageProvider(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(getProject(), 0));
		else
			pageProvider = new PageProvider(ProjectChildrenPage.class, ProjectChildrenPage.paramsOf(getProject()));
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Dashboard");
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
