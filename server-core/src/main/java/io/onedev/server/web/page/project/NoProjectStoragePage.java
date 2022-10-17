package io.onedev.server.web.page.project;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;

@SuppressWarnings("serial")
public class NoProjectStoragePage extends ProjectPage {

	public NoProjectStoragePage(PageParameters params) {
		super(params);
		
		if (getProject().getStorageServerUUID(false) != null) {
			throw new RestartResponseException(ProjectDashboardPage.class, 
					ProjectDashboardPage.paramsOf(getProject()));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("projectId", getProject().getId()));
	}

	@Override
	protected void navToProject(Project project) {
		if (project.isCodeManagement()) 
			setResponsePage(ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(project, 0));
		else
			setResponsePage(ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "No Storage");
	}

}
