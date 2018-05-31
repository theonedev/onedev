package io.onedev.server.exception;

public class ScriptException extends OneException {

	private static final long serialVersionUID = 1L;

	public ScriptException(String script, RuntimeException cause) {
		super("Error evaluating groovy script:\n" + script, cause);
	}
	
}
