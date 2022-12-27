package io.onedev.server.git.location;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Use curl in System Path", order=100)
public class SystemCurl extends CurlLocation {

	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "curl";
	}

}
