package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Editable(name="Issue list", order=200)
public class IssueListWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String baseQuery;
	
	@Editable(order=100, name="Project", placeholder="All accessible", description="Optionally specify project "
			+ "to show issues of. Leave empty to show issues of all accessible projects")
	@ProjectChoice("getPermittedProjects")
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@SuppressWarnings("unused")
	private static List<Project> getPermittedProjects() {
		List<Project> projects = new ArrayList<>(getProjectManager().getPermittedProjects(new AccessProject()));
		Collections.sort(projects, getProjectManager().cloneCache().comparingPath());
		return projects;
	}
	
	@Editable(order=200, placeholder="All issues", description="Optionally specify base query of the list")
	@IssueQuery(withCurrentUserCriteria=true)
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}
	
	@Override
	protected Component doRender(String componentId) {
		Long projectId;
		if (projectPath != null) {
			Project project = getProjectManager().findByPath(projectPath);
			if (project == null)
				throw new ExplicitException("Project not found: " + projectPath);
			else if (!SecurityUtils.canAccess(project))
				throw new ExplicitException("Permission denied");
			else
				projectId = project.getId();
		} else {
			projectId = null;
		}
		return new IssueListPanel(componentId, Model.of((String)null)) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Project getProject() {
				if (projectId != null)
					return getProjectManager().load(projectId);
				else
					return null;
			}

			@Override
			protected io.onedev.server.search.entity.issue.IssueQuery getBaseQuery() {
				IssueQueryParseOption option = new IssueQueryParseOption();
				option.withCurrentUserCriteria(true);
				return io.onedev.server.search.entity.issue.IssueQuery.parse(getProject(), baseQuery, option, true);
			}
			
		};
	}

}
