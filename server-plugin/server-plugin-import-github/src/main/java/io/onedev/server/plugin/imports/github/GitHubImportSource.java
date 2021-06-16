package io.onedev.server.plugin.imports.github;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GitHubImportSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String apiUrl = "https://api.github.com";
	
	private String accessToken;

	@Editable(order=10, name="GitHub API URL", description="Specify GitHub API url, for instance <tt>https://api.github.com</tt>")
	@NotEmpty
	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Editable(order=100, name="GitHub Personal Access Token", description="GitHub personal access token should be generated with "
			+ "scope <b>repo</b> and <b>read:org</b>")
	@NotEmpty
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getApiEndpoint(String apiPath) {
		return StringUtils.stripEnd(apiUrl, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
}
