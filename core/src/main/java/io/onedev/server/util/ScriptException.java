package io.onedev.server.util;

public class ScriptException extends OneException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String script, RuntimeException cause) {
		super("Error evaluating script:\n" + script, cause);
	}
	
}
