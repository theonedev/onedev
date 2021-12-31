package io.onedev.server.buildspec.step;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.step.commandinterpreter.DefaultInterpreter;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=160, name="Build/Publish Docker Image")
public class BuildImageStep extends CommandStep {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;
	
	private String tags;
	
	private RegistryLogin login;
	
	private boolean publish;
	
	@Editable(order=100, description="Optionally specify build path relative to <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use job workspace itself")
	@Interpolative(variableSuggester="suggestVariables")
	public String getBuildPath() {
		return buildPath;
	}

	public void setBuildPath(String buildPath) {
		this.buildPath = buildPath;
	}

	@Editable(order=200, description="Optionally specify Dockerfile relative to <a href='$docRoot/pages/concepts.md#job-workspace' target='_blank'>job workspace</a>. "
			+ "Leave empty to use file <tt>Dockerfile</tt> under job workspace")
	@Interpolative(variableSuggester="suggestVariables")
	public String getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(String dockerfile) {
		this.dockerfile = dockerfile;
	}

	@Editable(order=300, description="Specify tags of built image. Multiple tags should be separated with space")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Editable(order=350, name="Registry Login", description="Optionally specify docker registry login")
	public RegistryLogin getLogin() {
		return login;
	}

	public void setLogin(RegistryLogin login) {
		this.login = login;
	}

	@Editable(order=400, name="Publish After Build", description="Whether or not to publish built image to docker registry")
	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	@Override
	public String getImage() {
		return "docker";
	}

	@Editable
	@Override
	public boolean isRunInContainer() {
		return true;
	}

	@Override
	public Interpreter getInterpreter() {
		DefaultInterpreter interpreter = new DefaultInterpreter();
		
		StringBuilder buildCommand = new StringBuilder("docker build ");
		String[] parsedTags = StringUtils.parseQuoteTokens(getTags());
		for (String tag: parsedTags) 
			buildCommand.append("-t ").append(tag).append(" ");
		if (getDockerfile() != null)
			buildCommand.append("-f ").append("$workspace/" + getDockerfile());
		else
			buildCommand.append("-f ").append("$workspace/Dockerfile");
		
		buildCommand.append(" ");
		
		if (getBuildPath() != null)
			buildCommand.append("$workspace/" + getBuildPath());
		else
			buildCommand.append("$workspace");
		
		interpreter.getCommands().add("set -e");

		if (getLogin() != null) {
			StringBuilder loginCommand = new StringBuilder("echo ");
			loginCommand.append(getLogin().getPassword()).append("|docker login -u ");
			loginCommand.append(getLogin().getUserName()).append(" --password-stdin");
			if (getLogin().getRegistryUrl() != null)
				loginCommand.append(" ").append(getLogin().getRegistryUrl());
			interpreter.getCommands().add(loginCommand.toString());
		}
		
		interpreter.getCommands().add("workspace=$(pwd)");
		interpreter.getCommands().add(buildCommand.toString());
		
		if (isPublish()) {
			for (String tag: parsedTags)  
				interpreter.getCommands().add("docker push " + tag);
		}
		
		return interpreter;
	}

}
