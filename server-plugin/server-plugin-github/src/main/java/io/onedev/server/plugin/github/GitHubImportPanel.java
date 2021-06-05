package io.onedev.server.plugin.github;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import io.onedev.server.web.page.project.imports.OAuthAwareImportPanel;

@SuppressWarnings("serial")
public class GitHubImportPanel extends OAuthAwareImportPanel {

	private final GitHubSetting setting;
	
	public GitHubImportPanel(String id, GitHubSetting setting) {
		super(id);
		this.setting = setting;
	}

	@Override
	protected DefaultApi20 getApiEndpoint() {
		return GitHubApi.instance();
	}

	@Override
	protected String getApiKey() {
		return setting.getClientId();
	}

	@Override
	protected String getApiSecret() {
		return setting.getClientSecret();
	}

	@Override
	protected String getCallbackUrl() {
		return GitHubCallbackPage.getUrl();
	}

	@Override
	protected void onInitialize(OAuth20Service service, OAuth2AccessToken token) {
	}

}
