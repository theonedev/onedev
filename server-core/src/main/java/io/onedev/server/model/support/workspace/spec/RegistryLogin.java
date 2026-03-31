package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Password;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String password;

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
	
	@Editable(order=300, description = "Specify password or access token of the registry")
	@Password
	@NotEmpty
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}