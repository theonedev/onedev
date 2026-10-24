package io.onedev.server.git.location;

import java.io.Serial;

import io.onedev.server.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
public class SystemGit extends GitLocation {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		return "git";
	}

}
