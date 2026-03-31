package io.onedev.server.buildspec.step.commandinterpreter;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.InterpreterFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.BuildSpec;

import java.io.Serializable;
import java.util.List;

@Editable
public abstract class Interpreter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String commands;
	
	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}

	public abstract InterpreterFacade getFacade();
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, true);
	}
	
}
