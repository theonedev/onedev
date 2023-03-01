package io.onedev.server.plugin.imports.gitlab;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.ProjectPath;
import io.onedev.server.annotation.UrlPath;
import io.onedev.server.annotation.Editable;

@Editable
public class ProjectMapping implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_ONEDEV_PROJECT = "oneDevProject";
	
	private String gitLabProejct;
	
	private String oneDevProject;
	
	@Editable(order=100, name="GitLab Project", description="Specify GitLab project in form of "
			+ "<tt>group/subgroup/project</tt>")
	@UrlPath
	@NotEmpty
	public String getGitLabProject() {
		return gitLabProejct;
	}

	public void setGitLabProject(String gitLabProject) {
		this.gitLabProejct = gitLabProject;
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