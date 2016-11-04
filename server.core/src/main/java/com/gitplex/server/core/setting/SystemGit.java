package com.gitplex.server.core.setting;

import com.gitplex.commons.git.GitConfig;
import com.gitplex.commons.wicket.editable.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
@SuppressWarnings("serial")
public class SystemGit extends GitConfig {

	@Override
	public String getExecutable() {
		return "git";
	}

}
