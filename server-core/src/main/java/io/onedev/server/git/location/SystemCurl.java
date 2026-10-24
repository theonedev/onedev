package io.onedev.server.git.location;

import java.io.Serial;

import io.onedev.server.annotation.Editable;

@Editable(name="Use curl in System Path", order=100)
public class SystemCurl extends CurlLocation {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "curl";
	}

}
