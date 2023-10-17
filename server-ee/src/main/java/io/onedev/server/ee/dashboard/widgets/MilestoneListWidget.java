package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.MilestoneSort;
import io.onedev.server.web.component.milestone.list.MilestoneListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Editable(order=210, name="Milestone list")
public class MilestoneListWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	@Editable(order=100, name="Project")
	@ProjectChoice("getPermittedProjects")
	@NotEmpty
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
	
	@Override
	protected Component doRender(String componentId) {
		Long projectId;
		Project project = getProjectManager().findByPath(projectPath);
		if (project == null)
			throw new ExplicitException("Project not found: " + projectPath);
		else
			projectId = project.getId();
		
		if (SecurityUtils.canAccess(project)) {
			return new MilestoneListPanel(componentId, new LoadableDetachableModel<Project>() {

				private static final long serialVersionUID = 1L;

				@Override
				protected Project load() {
					return getProjectManager().load(projectId);
				}
				
			}, false, MilestoneSort.CLOSEST_DUE_DATE, null);
		} else {
			throw new ExplicitException("Permission denied");
		}
	}

}
