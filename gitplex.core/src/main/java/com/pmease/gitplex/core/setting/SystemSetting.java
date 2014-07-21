package com.pmease.gitplex.core.setting;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.validation.Directory;

@SuppressWarnings("serial")
@Editable
public class SystemSetting implements Serializable {
	
	private String repoPath;
	
	private GitConfig gitConfig = new SystemGit();
	
	private boolean gravatarEnabled = true;
	
	@Editable(name="Directory to Store Repositories", order=100, description="Specify directory to store Git repositories.")
	@Directory
	@NotEmpty
	public String getRepoPath() {
		return repoPath;
	}

	public void setRepoPath(String repoPath) {
		this.repoPath = repoPath;
	}

	@Editable(order=200, description="GitPlex relies on git command line to operate managed repositories. The minimum "
			+ "required version is 1.8.0.")
	@Valid
	@NotNull
	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConfig) {
		this.gitConfig = gitConfig;
	}

	@Editable(order=300, description="Whether or not to enable user gravatar.")
	public boolean isGravatarEnabled() {
		return gravatarEnabled;
	}

	public void setGravatarEnabled(boolean gravatarEnabled) {
		this.gravatarEnabled = gravatarEnabled;
	}

}
