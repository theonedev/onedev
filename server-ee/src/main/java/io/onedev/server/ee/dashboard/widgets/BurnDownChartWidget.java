package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.MilestoneChoice;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.component.milestone.burndown.BurndownIndicators;
import io.onedev.server.web.component.milestone.burndown.MilestoneBurndownPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Editable(order=250, name="Burndown chart")
public class BurnDownChartWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String milestoneName;
	
	private String indicator;
	
	@Editable(order=100, name="Project")
	@ProjectChoice("getPermittedProjects")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	private static List<Project> getPermittedProjects() {
		List<Project> projects = new ArrayList<>(getProjectManager().getPermittedProjects(new AccessProject()));
		Collections.sort(projects, getProjectManager().cloneCache().comparingPath());
		return projects;
	}

	@Editable(order=200, name="Milestone")
	@MilestoneChoice("getMilestoneChoices")
	@NotEmpty
	public String getMilestoneName() {
		return milestoneName;
	}

	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	private static List<Milestone> getMilestoneChoices() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null) {
			try {
				return getProject(projectPath).getSortedHierarchyMilestones();
			} catch (ExplicitException e) {
				return new ArrayList<>();
			}
		} else {
			return new ArrayList<>();
		}
	}
	
	@Editable(order=300)
	@ChoiceProvider("getIndicatorChoices")
	@NotEmpty
	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}
	
	private static List<String> getIndicatorChoices() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null) {
			try {
				return BurndownIndicators.getChoices(getProject(projectPath));
			} catch (ExplicitException e) {
				return new ArrayList<>();
			}
		} else {
			return new ArrayList<>();
		}
	}
	
	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@SuppressWarnings("unused")
	private static Project getProject(String projectPath) {
		Project project = getProjectManager().findByPath(projectPath);
		if (project == null)
			throw new ExplicitException("Project not found: " + projectPath);
		if (SecurityUtils.canAccess(project)) 
			return project;
		else 
			throw new ExplicitException("Permission denied");
	}
	
	@Override
	protected Component doRender(String componentId) {
		Project project = getProject(projectPath);
		var milestone = project.getHierarchyMilestone(milestoneName);
		if (milestone == null)
			throw new ExplicitException("Milestone not found: " + milestoneName);
		var milestoneId = milestone.getId();
		return new MilestoneBurndownPanel(componentId, new LoadableDetachableModel<Milestone>() {
			@Override
			protected Milestone load() {
				return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
			}
		}, indicator);
	}

}
