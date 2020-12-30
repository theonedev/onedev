package io.onedev.server.util;

import io.onedev.commons.utils.ExplicitException;

public class ScriptException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String script, RuntimeException cause) {
		super("Error evaluating groovy script:\n\n" + script, cause);
	}
	
}
