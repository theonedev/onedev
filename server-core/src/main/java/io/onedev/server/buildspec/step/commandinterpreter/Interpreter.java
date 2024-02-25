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
	
	private String commands;
	
	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}

	public abstract CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, @Nullable String image,
												@Nullable String builtInRegistryAccessToken, boolean useTTY);
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, true);
	}
	
}
