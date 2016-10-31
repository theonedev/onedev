package com.gitplex.core.setting;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.commons.git.GitConfig;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="Use Specified Git", order=200)
@SuppressWarnings("serial")
public class SpecifiedGit extends GitConfig {

	private String gitPath;
	
	@Editable(description="Specify path to git executable, for instance: <tt>/usr/bin/git</tt>.")
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
