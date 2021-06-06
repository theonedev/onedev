package io.onedev.server.plugin.imports.github;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GitHubImport implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_PROJECT_NAME = "projectName";
	
	private String githubRepo;
	
	private String projectName;
	
	private boolean importIssues = true;
	
	@Editable(order=100, name="Repository")
	@NotEmpty
	public String getGithubRepo() {
		return githubRepo;
	}

	public void setGithubRepo(String githubRepo) {
		this.githubRepo = githubRepo;
	}

	@Editable(order=200, name="Import as Project")
	@NotEmpty
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Editable(order=400)
	public boolean isImportIssues() {
		return importIssues;
	}

	public void setImportIssues(boolean importIssues) {
		this.importIssues = importIssues;
	}

}