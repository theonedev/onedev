package com.gitplex.server.git.config;

import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Use curl in System Path", order=100)
@SuppressWarnings("serial")
public class SystemCurl extends CurlConfig {

	@Override
	public String getExecutable() {
		return "curl";
	}

}
