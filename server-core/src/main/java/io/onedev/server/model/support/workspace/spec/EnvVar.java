package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.EnvVarName;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class EnvVar implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private boolean secret;

	private String value;

	private String secretValue;

	@Editable(order=100, description="Specify name of the environment variable")
	@Interpolative(variableSuggester="suggestVariables")
	@EnvVarName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=150, description="Whether or not the value is secret")
	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	@Editable(order=200, description="Either specify value or secret value of the environment variable")
	@DependsOn(property="secret", value="false")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable(order=300, description="Either specify value or secret value of the environment variable")
	@DependsOn(property="secret", value="true")
	@Password
	@NotEmpty
	public String getSecretValue() {
		return secretValue;
	}

	public void setSecretValue(String secretValue) {
		this.secretValue = secretValue;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
