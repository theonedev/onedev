package com.gitplex.server.git.config;

import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Use git in System Path", order=100)
public class SystemGit extends GitConfig {

	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "git";
	}

}
