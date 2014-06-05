package com.pmease.gitop.core.setting;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.GitConfig;

@Editable(name="Use Git in System Path", order=100)
@SuppressWarnings("serial")
public class SystemGit extends GitConfig {

	@Override
	public String getExecutable() {
		return "git";
	}

}
