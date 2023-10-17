package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.BuildQuery;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.web.component.build.list.BuildListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Editable(name="Build list", order=400)
public class BuildListWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String baseQuery;
	
	private boolean showJob = true;
	
	private boolean showRef = true;
	
	@Editable(order=100, name="Project", placeholder="All accessible", description="Optionally specify project to "
			+ "show builds of. Leave empty to show builds of all projects with permissions")
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
	
	@Editable(order=200, placeholder="All builds", description="Optionally specify base query of the list")
	@BuildQuery(withCurrentUserCriteria=true)
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	@Editable(order=300, description="Whether or not to show job column")
	public boolean isShowJob() {
		return showJob;
	}

	public void setShowJob(boolean showJob) {
		this.showJob = showJob;
	}

	@Editable(order=300, name="Show branch/tag", description="Whether or not to show branch/tag column")
	public boolean isShowRef() {
		return showRef;
	}

	public void setShowRef(boolean showRef) {
		this.showRef = showRef;
	}
	
	@Override
	protected Component doRender(String componentId) {
		Long projectId;
		if (projectPath != null) {
			Project project = getProjectManager().findByPath(projectPath);
			if (project == null)
				throw new ExplicitException("Project not found: " + projectPath);
			else
				projectId = project.getId();
		} else {
			projectId = null;
		}
		return new BuildListPanel(componentId, Model.of((String)null), showJob, showRef, 0) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Project getProject() {
				if (projectId != null)
					return getProjectManager().load(projectId);
				else
					return null;
			}

			@Override
			protected io.onedev.server.search.entity.build.BuildQuery getBaseQuery() {
				return io.onedev.server.search.entity.build.BuildQuery.parse(getProject(), baseQuery, true, true);
			}
			
		};
	}

}
