package com.gitplex.server.git.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.util.editable.annotation.Editable;

@Editable(name="Use Specified curl", order=200)
@SuppressWarnings("serial")
public class SpecifiedCurl extends CurlConfig {

	private String curlPath;
	
	@Editable(description="Specify path to curl executable, for instance: <tt>/usr/bin/curl</tt>")
	@NotEmpty
	public String getCurlPath() {
		return curlPath;
	}

	public void setCurlPath(String curlPath) {
		this.curlPath = curlPath;
	}

	@Override
	public String getExecutable() {
		return curlPath;
	}

}
