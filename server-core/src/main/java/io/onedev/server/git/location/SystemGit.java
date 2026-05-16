package io.onedev.server.git.location;

import io.onedev.server.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
public class SystemGit extends GitLocation {

	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "git";
	}

}
