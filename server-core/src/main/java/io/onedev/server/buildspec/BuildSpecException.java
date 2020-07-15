package io.onedev.server.buildspec;

import io.onedev.server.GeneralException;
import io.onedev.server.util.Path;

public class BuildSpecException extends GeneralException {

	private static final long serialVersionUID = 1L;

	public BuildSpecException(Path path, String message) {
		super(path.toString() + ": " + message);
	}
	
}
