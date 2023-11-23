package io.onedev.server.ee.dashboard.widgets;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.web.component.pack.list.PackListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Editable(name="Package list", order=450)
public class PackListWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String projectPath;
	
	private String packType;
	
	@Editable(order=100, name="Project", placeholder="All accessible", description="Optionally specify project to "
			+ "show packages of. Leave empty to show packages of all projects with permissions")
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

	@Editable
	@ChoiceProvider("getTypeChoices")
	@NotEmpty
	public String getPackType() {
		return packType;
	}

	public void setPackType(String packType) {
		this.packType = packType;
	}

	private static List<String> getTypeChoices() {
		var packSupports = new ArrayList<>(OneDev.getExtensions(PackSupport.class));
		return packSupports.stream().sorted(comparing(PackSupport::getOrder))
				.map(PackSupport::getPackType).collect(toList());
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
		return new PackListPanel(componentId, Model.of((String)null), false) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Project getProject() {
				if (projectId != null)
					return getProjectManager().load(projectId);
				else
					return null;
			}

			@Nullable
			@Override
			protected String getPackType() {
				return packType;
			}
			
		};
	}

}
