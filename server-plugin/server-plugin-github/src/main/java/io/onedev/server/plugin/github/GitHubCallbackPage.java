package io.onedev.server.plugin.github;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.page.admin.sso.SsoProcessPage;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class GitHubCallbackPage extends BasePage {

	public static final String MOUNT_PATH = "github-callback";
	
	public GitHubCallbackPage(PageParameters params) {
		super(params);
		
		SsoProcessPage.addParams(params, SsoProcessPage.STAGE_CALLBACK, GitHubPluginModule.SSO_CONNECTOR_NAME); 
		PageProvider pageProvider = new PageProvider(SsoProcessPage.class, params);
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

}
