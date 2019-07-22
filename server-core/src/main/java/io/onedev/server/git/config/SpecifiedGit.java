package io.onedev.server.git.config;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Use Specified git", order=200)
public class SpecifiedGit extends GitConfig {

	private static final long serialVersionUID = 1L;
	
	private String gitPath;
	
	@Editable(description="Specify path to git executable, for instance: <tt>/usr/bin/git</tt>")
	@NotEmpty
	public String getGitPath() {
		return gitPath;
	}

	public void setGitPath(String gitPath) {
		this.gitPath = gitPath;
	}

	@Override
	public String getExecutable() {
		return gitPath;
	}

}
