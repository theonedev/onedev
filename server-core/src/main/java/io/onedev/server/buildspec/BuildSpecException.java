package io.onedev.server.buildspec;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.Path;

public class BuildSpecException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public BuildSpecException(Path path, String message) {
		super(path.toString() + ": " + message);
	}
	
}
