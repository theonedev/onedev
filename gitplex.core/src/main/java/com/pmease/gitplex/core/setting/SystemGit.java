package com.pmease.gitplex.core.setting;

import com.pmease.commons.git.GitConfig;
import com.pmease.commons.wicket.editable.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
@SuppressWarnings("serial")
public class SystemGit extends GitConfig {

	@Override
	public String getExecutable() {
		return "git";
	}

}
