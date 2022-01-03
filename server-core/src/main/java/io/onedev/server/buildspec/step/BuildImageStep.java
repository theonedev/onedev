package io.onedev.server.buildspec.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.StringUtils;
import io.onedev.k8shelper.CommandExecutable;
import io.onedev.k8shelper.Executable;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.OsExecution;
import io.onedev.k8shelper.OsInfo;
import io.onedev.k8shelper.OsMatcher;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=160, name="Build/Publish Docker Image")
public class BuildImageStep extends Step {

	private static final long serialVersionUID = 1L;

	private String buildPath;
	
	private String dockerfile;
	
	private String tags;
	
	private RegistryLogin login;
	
	private boolean publish;
	
	private boolean useTTY;
	
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

	@Editable(order=300, description="Name and optionally a tag in the 'name:tag' format. Multiple tags should be separated with space")
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

	@Editable(order=10000, name="Enable TTY Mode", description=CommandStep.USE_TTY_HELP)
	public boolean isUseTTY() {
		return useTTY;
	}

	public void setUseTTY(boolean useTTY) {
		this.useTTY = useTTY;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	private List<String> getNonWindowsCommands() {
		List<String> commands = new ArrayList<>();
		
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
		
		commands.add("set -e");

		if (getLogin() != null) {
			StringBuilder loginCommand = new StringBuilder("echo ");
			loginCommand.append(getLogin().getPassword()).append("|docker login -u ");
			loginCommand.append(getLogin().getUserName()).append(" --password-stdin");
			if (getLogin().getRegistryUrl() != null)
				loginCommand.append(" ").append(getLogin().getRegistryUrl());
			commands.add(loginCommand.toString());
		}
		
		commands.add("workspace=$(pwd)");
		commands.add(buildCommand.toString());
		
		if (isPublish()) {
			for (String tag: parsedTags)  
				commands.add("docker push " + tag);
		}
		
		return commands;
	}

	private List<String> getWindowsCommands() {
		List<String> commands = new ArrayList<>();
		
		StringBuilder buildCommand = new StringBuilder("docker build ");
		String[] parsedTags = StringUtils.parseQuoteTokens(getTags());
		for (String tag: parsedTags) 
			buildCommand.append("-t ").append(tag).append(" ");
		if (getDockerfile() != null)
			buildCommand.append("-f ").append("%workspace%\\" + getDockerfile().replace('/', '\\'));
		else
			buildCommand.append("-f ").append("%workspace%\\Dockerfile");
		
		buildCommand.append(" ");
		
		if (getBuildPath() != null)
			buildCommand.append("%workspace%\\" + getBuildPath().replace('/', '\\'));
		else
			buildCommand.append("%workspace%");
		
		buildCommand.append(" || exit /b 1");
		
		commands.add("@echo off");

		if (getLogin() != null) {
			StringBuilder loginCommand = new StringBuilder("echo ");
			loginCommand.append(getLogin().getPassword()).append("|docker login -u ");
			loginCommand.append(getLogin().getUserName()).append(" --password-stdin || exit /b 1");
			if (getLogin().getRegistryUrl() != null)
				loginCommand.append(" ").append(getLogin().getRegistryUrl());
			commands.add(loginCommand.toString());
		}
		
		commands.add("set workspace=%cd%");
		commands.add(buildCommand.toString());
		
		if (isPublish()) {
			for (String tag: parsedTags)  
				commands.add("docker push " + tag + " || exit /b 1");
		}
		
		return commands;
	}
	
	@Override
	public Executable getExecutable(Build build, String jobToken, ParamCombination paramCombination) {
		List<OsExecution> executions = new ArrayList<>();
		String imageVersion = KubernetesHelper.getVersion();
		
		String image = KubernetesHelper.IMAGE_REPO_PREFIX + "-linux:" + imageVersion;
		executions.add(new OsExecution(OsMatcher.NON_WINDOWS, image, getNonWindowsCommands()));
		
		List<String> commands = getWindowsCommands();
		for (Map.Entry<Integer, String> entry: OsInfo.WINDOWS_VERSIONS.entrySet()) {
			OsMatcher osMatcher = new OsMatcher("(?i).*windows.*", ".*\\." + entry.getKey(), ".*");
			image = KubernetesHelper.IMAGE_REPO_PREFIX 
					+ "-windows-" + entry.getValue().toLowerCase() 
					+ ":" + imageVersion;
			executions.add(new OsExecution(osMatcher, image, commands));
		}	
		
		OsMatcher osMatcher = new OsMatcher("(?i).*windows.*", ".*", ".*");
		String windowsImage = KubernetesHelper.IMAGE_REPO_PREFIX 
				+ "-windows-1809" + ":" + imageVersion;
		executions.add(new OsExecution(osMatcher, windowsImage, commands));
		
		return new CommandExecutable(executions, isUseTTY());
	}

}
