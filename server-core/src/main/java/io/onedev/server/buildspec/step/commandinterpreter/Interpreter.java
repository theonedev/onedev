package io.onedev.server.buildspec.step.commandinterpreter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CommandFacade;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;

@Editable
public abstract class Interpreter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<String> commands = new ArrayList<>();
	
	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public abstract CommandFacade getExecutable(JobExecutor jobExecutor, @Nullable String image, boolean useTTY);
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, true);
	}
	
}
