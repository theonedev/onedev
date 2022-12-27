package io.onedev.server.git.location;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Use Git in System Path", order=100)
public class SystemGit extends GitLocation {

	private static final long serialVersionUID = 1L;

	@Override
	public String getExecutable() {
		if (SystemUtils.IS_OS_MAC_OSX && new File("/usr/local/bin/git").exists())
			return "/usr/local/bin/git";
		else
			return "git";
	}

}
