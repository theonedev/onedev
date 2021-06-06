package io.onedev.server.plugin.imports.github;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class GitHubImportSource implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String apiServer = "https://api.github.com";
	
	private String accessToken;

	@Editable(order=10)
	@NotEmpty
	public String getApiServer() {
		return apiServer;
	}

	public void setApiServer(String apiServer) {
		this.apiServer = apiServer;
	}

	@Editable(order=100, name="Personal Access Token", description="GitHub personal access token should be generated with "
			+ "scope 'repo' and 'read:org'")
	@NotEmpty
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getApiUrl(String apiPath) {
		return StringUtils.stripEnd(apiServer, "/") + "/" + StringUtils.stripStart(apiPath, "/");
	}
	
}
