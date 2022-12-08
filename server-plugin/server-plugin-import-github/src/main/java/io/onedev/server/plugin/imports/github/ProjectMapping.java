package io.onedev.server.plugin.imports.github;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.validation.annotation.ProjectPath;
import io.onedev.server.util.validation.annotation.UrlPath;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String gitHubRepo;
	
	private String oneDevProject;
	
	@Editable(order=100, name="GitHub Repository", description="Specify GitHub repository in form of "
			+ "<tt>organization/repository</tt>")
	@UrlPath
	@NotEmpty
	public String getGitHubRepo() {
		return gitHubRepo;
	}

	public void setGitHubRepo(String gitHubRepo) {
		this.gitHubRepo = gitHubRepo;
	}

	@Editable(order=200, name="OneDev Project", description="Specify OneDev project to import into")
	@ProjectPath
	@NotEmpty
	public String getOneDevProject() {
		return oneDevProject;
	}

	public void setOneDevProject(String oneDevProject) {
		this.oneDevProject = oneDevProject;
	}

}