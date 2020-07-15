package io.onedev.server.util;

import io.onedev.server.GeneralException;

public class ScriptException extends GeneralException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String script, RuntimeException cause) {
		super("Error evaluating groovy script:\n\n" + script, cause);
	}
	
}
