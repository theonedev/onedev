package io.onedev.server.buildspec.step;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.RegistryLoginFacade;
import io.onedev.server.annotation.*;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String passwordSecret;

	@Editable(order=100, placeholder="Docker Hub", displayPlaceholderAsValue =true, description="Specify registry url. Leave empty for official registry")
	@Interpolative(variableSuggester = "suggestVariables")
	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	@Editable(order=200, description = "Specify user name of the registry")
	@Interpolative(variableSuggester = "suggestVariables")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	@Editable(order=300, description = "Specify a <a href='https://docs.onedev.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as password or access token of the registry")
	@ChoiceProvider("getPasswordSecretChoices")
	@NotEmpty
	public String getPasswordSecret() {
		return passwordSecret;
	}

	public void setPasswordSecret(String passwordSecret) {
		this.passwordSecret = passwordSecret;
	}

	protected static List<String> getPasswordSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	public RegistryLoginFacade getFacade(Build build) {
		var password = build.getJobAuthorizationContext().getSecretValue(getPasswordSecret());
		return new RegistryLoginFacade(getRegistryUrl(), getUserName(), password);
	}
	
}