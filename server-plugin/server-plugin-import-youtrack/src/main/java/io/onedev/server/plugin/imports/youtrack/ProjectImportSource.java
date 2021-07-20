package io.onedev.server.plugin.imports.youtrack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectImportSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<ProjectMapping> projectMappings = new ArrayList<>();

	@Editable(order=100, name="Projects to Import")
	public List<ProjectMapping> getProjectMappings() {
		return projectMappings;
	}

	public void setProjectMappings(List<ProjectMapping> projectMappings) {
		this.projectMappings = projectMappings;
	}

}
