package io.onedev.server.plugin.imports.jiracloud;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.ProjectPath;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String jiraProject;
	
	private String oneDevProject;
	
	@Editable(order=100, name="JIRA Project", description="Specify JIRA project to be imported")
	@NotEmpty
	public String getJiraProject() {
		return jiraProject;
	}

	public void setJiraProject(String jiraProject) {
		this.jiraProject = jiraProject;
	}

	@Editable(order=200, name="OneDev Project", description="Specify OneDev project to be created as "
			+ "result of importing")
	@ProjectPath
	@NotEmpty
	public String getOneDevProject() {
		return oneDevProject;
	}

	public void setOneDevProject(String oneDevProject) {
		this.oneDevProject = oneDevProject;
	}

}