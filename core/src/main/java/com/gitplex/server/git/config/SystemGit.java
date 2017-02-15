package com.gitplex.server.git.config;

import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
@SuppressWarnings("serial")
public class SystemGit extends GitConfig {

	@Override
	public String getExecutable() {
		return "git";
	}

}
