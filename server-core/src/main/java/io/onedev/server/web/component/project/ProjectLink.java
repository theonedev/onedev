package io.onedev.server.web.component.project;

import org.apache.wicket.Page;

import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.issues.list.IssueListPage;

public class ProjectLink extends ViewStateAwarePageLink<Void> {

	private static final long serialVersionUID = 1L;

	public ProjectLink(String id, Project project) {
		super(id, getPageClass(project), ProjectPage.paramsOf(project));
	}

	private static Class<? extends Page> getPageClass(Project project) {
		if (SecurityUtils.canReadCode(project.getFacade()))
			return ProjectBlobPage.class;
		else
			return IssueListPage.class;
	}
}
